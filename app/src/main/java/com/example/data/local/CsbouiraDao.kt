package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CsbouiraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :fileId")
    suspend fun deleteBookmark(fileId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :fileId)")
    suspend fun isBookmarked(fileId: String): Boolean

    @Query("SELECT * FROM bookmarks")
    fun getBookmarkedFilesFlow(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :fileId")
    suspend fun getBookmarkById(fileId: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CacheEntity)

    @Query("SELECT * FROM cache WHERE `key` = :key")
    suspend fun getCache(key: String): CacheEntity?

    @Query("DELETE FROM cache WHERE `key` = :key")
    suspend fun deleteCache(key: String)

    @Query("DELETE FROM cache")
    suspend fun clearCache()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE url = :url")
    suspend fun getDownload(url: String): DownloadEntity?

    @Query("SELECT * FROM downloads")
    fun getDownloadsFlow(): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE url = :url")
    suspend fun deleteDownload(url: String)
}
