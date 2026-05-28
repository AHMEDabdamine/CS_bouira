package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.HardcodedData
import com.example.data.api.CsbouiraApi
import com.example.data.api.DriveFile
import com.example.data.api.DriveResponse
import com.example.data.local.BookmarkEntity
import com.example.data.local.CacheEntity
import com.example.data.local.CsbouiraDao
import com.example.data.local.DownloadEntity
import com.example.data.model.FileItem
import com.example.data.model.Module
import com.example.data.model.Year
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class CsbouiraRepository(
    private val api: CsbouiraApi,
    private val dao: CsbouiraDao,
    private val appContext: Context
) {
    private val gson = Gson()
    private val yearCache = mutableMapOf<String, DriveResponse>()
    private val cacheTtlMs = 60 * 60 * 1000L

    private val downloadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun preloadYear(yearName: String) {
        fetchYear(yearName)
    }

    suspend fun clearAllCache() {
        yearCache.clear()
        dao.clearCache()
    }

    private suspend fun fetchYear(yearName: String): DriveResponse? {
        yearCache[yearName]?.let { return it }

        val cacheKey = "year_$yearName"
        val cached = dao.getCache(cacheKey)
        if (cached != null && System.currentTimeMillis() - cached.timestamp < cacheTtlMs) {
            val parsed: DriveResponse = gson.fromJson(cached.json, DriveResponse::class.java)
            yearCache[yearName] = parsed
            return parsed
        }

        return try {
            val resp = api.getDriveYear(yearName)
            val type = object : TypeToken<DriveResponse>() {}.type
            val driveResp: DriveResponse = gson.fromJson(resp, type)
            val jsonStr = gson.toJson(driveResp)
            dao.insertCache(CacheEntity(cacheKey, jsonStr, System.currentTimeMillis()))
            yearCache[yearName] = driveResp
            driveResp
        } catch (e: Exception) {
            cached?.let {
                val parsed: DriveResponse = gson.fromJson(it.json, DriveResponse::class.java)
                yearCache[yearName] = parsed
                return parsed
            }
            null
        }
    }

    fun getYearsFlow(): Flow<List<Year>> = flow {
        val years = HardcodedData.yearOrder.map { name ->
            Year(
                id = HardcodedData.getYearId(name),
                name = name,
                description = HardcodedData.getYearDescription(name)
            )
        }
        emit(years)
    }

    fun getModulesFlow(yearName: String): Flow<List<Module>> = flow {
        val yearResp = fetchYear(yearName) ?: run { emit(emptyList()); return@flow }
        val yearId = HardcodedData.getYearId(yearName)
        val modules = mutableListOf<Module>()
        for ((semKey, semFolder) in yearResp.subfolders.orEmpty()) {
            val semester = HardcodedData.getSemester(semKey) ?: continue
            for ((modName, _) in semFolder.subfolders.orEmpty()) {
                modules.add(
                    Module(
                        id = "${yearId}_${modName.replace(" ", "_")}_S${semester}",
                        name = modName, code = modName,
                        yearId = yearId, semester = semester
                    )
                )
            }
        }
        modules.sortBy { it.semester }
        emit(modules)
    }

    fun getFilesFlow(yearName: String, semester: Int, moduleName: String): Flow<List<FileItem>> = flow {
        val yearResp = fetchYear(yearName) ?: run { emit(emptyList()); return@flow }
        val semKey = yearResp.subfolders?.entries?.find { (key, _) ->
            HardcodedData.getSemester(key) == semester
        }?.key ?: run { emit(emptyList()); return@flow }
        val modFolder = yearResp.subfolders?.get(semKey)?.subfolders?.get(moduleName)
            ?: run { emit(emptyList()); return@flow }

        val files = mutableListOf<FileItem>()
        var fileIndex = 0
        for ((catNameRaw, catFolder) in modFolder.subfolders.orEmpty()) {
            val catName = catNameRaw.replace(" (empty)", "").trim()
            val fileType = HardcodedData.categoryTypes[catName] ?: continue
            val driveFiles = collectFilesRecursive(catFolder)
            for (df in driveFiles) {
                files.add(FileItem(
                    id = "file_${fileIndex++}",
                    moduleId = moduleName,
                    name = df.name,
                    type = fileType,
                    url = df.previewLink,
                    downloadUrl = df.downloadLink,
                    size = null,
                    uploadedAt = null,
                    uploader = null
                ))
            }
        }
        emit(files)
    }

    fun searchFilesFlow(query: String): Flow<List<FileItem>> = flow {
        if (query.isBlank()) { emit(emptyList()); return@flow }
        val terms = query.lowercase().split(" ").filter { it.isNotBlank() }
        val allFiles = mutableListOf<FileItem>()

        for (yearName in HardcodedData.yearOrder) {
            val yearResp = fetchYear(yearName) ?: continue
            for ((semKey, semFolder) in yearResp.subfolders.orEmpty()) {
                if (HardcodedData.getSemester(semKey) == null) continue
                for ((modName, modFolder) in semFolder.subfolders.orEmpty()) {
                    for ((catNameRaw, catFolder) in modFolder.subfolders.orEmpty()) {
                        val catName = catNameRaw.replace(" (empty)", "").trim()
                        val fileType = HardcodedData.categoryTypes[catName] ?: continue
                        for (df in collectFilesRecursive(catFolder)) {
                            allFiles.add(FileItem(
                                id = "${yearName}_${semKey}_${modName}_${df.name}",
                                moduleId = modName, name = df.name,
                                type = fileType, url = df.previewLink,
                                downloadUrl = df.downloadLink,
                                size = null, uploadedAt = null, uploader = null
                            ))
                        }
                    }
                }
            }
        }

        val filtered = allFiles.filter { file ->
            terms.all { term ->
                file.name.lowercase().contains(term) || file.type.lowercase().contains(term)
            }
        }
        emit(filtered)
    }

    suspend fun toggleBookmark(fileItem: FileItem) {
        if (dao.isBookmarked(fileItem.id)) {
            dao.deleteBookmark(fileItem.id)
        } else {
            dao.insertBookmark(BookmarkEntity(
                id = fileItem.id,
                moduleId = fileItem.moduleId,
                name = fileItem.name,
                type = fileItem.type,
                url = fileItem.url
            ))
        }
    }

    suspend fun isBookmarked(fileId: String): Boolean = dao.isBookmarked(fileId)

    fun getBookmarkedFiles(): Flow<List<FileItem>> {
        return dao.getBookmarkedFilesFlow().map { list ->
            list.map { FileItem(it.id, it.moduleId, it.name, it.type, it.url, size = null, uploadedAt = null, uploader = null) }
        }
    }

    suspend fun downloadFile(fileItem: FileItem, onProgress: (Float) -> Unit = {}): String? {
        val existing = dao.getDownload(fileItem.url)
        if (existing != null) {
            val f = File(existing.filePath)
            if (f.exists()) return existing.filePath
            dao.deleteDownload(fileItem.url)
        }

        val fileName = fileItem.name.ifBlank { "document.pdf" }

        return withContext(Dispatchers.IO) {
            try {
                val downloadUrl = toDownloadUrl(fileItem.downloadUrl.ifEmpty { fileItem.url })
                val fileBytes = downloadWithRedirectHandling(downloadUrl, onProgress) ?: return@withContext null

                // Always save to internal storage first (no permissions needed, always works)
                val saveDir = File(appContext.filesDir, "downloads")
                saveDir.mkdirs()
                val localFile = File(saveDir, fileName)
                localFile.writeBytes(fileBytes)

                // Also save to public Downloads folder for user visibility (best-effort)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val mimeType = when (fileName.substringAfterLast('.', "").lowercase()) {
                            "pdf" -> "application/pdf"
                            "jpg", "jpeg" -> "image/jpeg"
                            "png" -> "image/png"
                            "gif" -> "image/gif"
                            "doc", "docx" -> "application/msword"
                            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                            "xls", "xlsx" -> "application/vnd.ms-excel"
                            "zip" -> "application/zip"
                            "mp4" -> "video/mp4"
                            "mp3" -> "audio/mpeg"
                            "txt" -> "text/plain"
                            else -> "application/octet-stream"
                        }

                        val contentValues = ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                            put(MediaStore.Downloads.MIME_TYPE, mimeType)
                            put(MediaStore.Downloads.IS_PENDING, 0)
                        }

                        val resolver = appContext.contentResolver
                        val publicUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        if (publicUri != null) {
                            resolver.openOutputStream(publicUri)?.use { output ->
                                output.write(fileBytes)
                            }
                        }
                    } catch (_: Exception) { }
                } else {
                    try {
                        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val appDir = File(dir, "CSBouira")
                        appDir.mkdirs()
                        val publicFile = File(appDir, fileName)
                        publicFile.writeBytes(fileBytes)
                    } catch (_: Exception) { }
                }

                dao.insertDownload(DownloadEntity(fileItem.url, localFile.absolutePath, fileName, System.currentTimeMillis()))
                localFile.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getDownloadedPath(fileUrl: String): String? {
        return dao.getDownload(fileUrl)?.let {
            val f = File(it.filePath)
            if (f.exists()) it.filePath else null
        }
    }

    fun getDownloadsFlow(): Flow<List<DownloadEntity>> = dao.getDownloadsFlow()

    suspend fun deleteDownload(fileUrl: String) {
        dao.getDownload(fileUrl)?.let {
            File(it.filePath).delete()
            dao.deleteDownload(fileUrl)
        }
    }

    private fun collectFilesRecursive(folder: DriveResponse): List<DriveFile> {
        val result = mutableListOf<DriveFile>()
        result.addAll(folder.files.orEmpty())
        for ((_, sub) in folder.subfolders.orEmpty()) {
            result.addAll(collectFilesRecursive(sub))
        }
        return result
    }

    private fun downloadWithRedirectHandling(url: String, onProgress: (Float) -> Unit): ByteArray? {
        var currentUrl = url
        repeat(3) {
            val request = Request.Builder().url(currentUrl).build()
            val response = downloadClient.newCall(request).execute()
            val body = response.body ?: return@repeat
            val contentType = body.contentType()

            // Google Drive warning pages always have Content-Type: text/html
            val isWarningPage = contentType?.let { it.type == "text" && it.subtype == "html" } == true

            if (!isWarningPage) {
                val bytes = body.bytes()
                if (bytes.isNotEmpty()) {
                    onProgress(1f)
                    response.close()
                    return bytes
                }
                response.close()
                return null
            }

            // Extract confirm token from the warning page and retry
            val htmlStr = body.string()
            val confirmMatch = Regex("""confirm=([a-zA-Z0-9_-]+)""").find(htmlStr)
            val id = url.substringAfter("id=").substringBefore("&").ifEmpty {
                url.substringAfter("id=")
            }
            currentUrl = "https://drive.google.com/uc?export=download" +
                "&confirm=${confirmMatch?.groupValues?.getOrNull(1) ?: "t"}&id=$id"

            response.close()
        }
        return null
    }

    companion object {
        fun toDownloadUrl(url: String): String {
            val fileId = url.substringAfter("/d/").substringBefore("/")
            if (fileId.isNotBlank() && url.contains("drive.google.com")) {
                return "https://drive.google.com/uc?export=download&id=$fileId"
            }
            return url
        }
    }
}
