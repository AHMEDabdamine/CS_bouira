package com.example.ui.screens.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.components.IconBadge
import com.example.ui.components.ShimmerBox
import com.example.ui.theme.*

private data class FileTypeInfo(
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val bg: Color
)

private val fileTypeMap = mapOf(
    "course" to FileTypeInfo("COURS", Icons.Default.MenuBook, CourseAccent, CourseBg),
    "exams" to FileTypeInfo("EXAMENS", Icons.Default.Quiz, ExamAccent, ExamBg),
    "TD&TP" to FileTypeInfo("TD / TP", Icons.Default.Handyman, TdAccent, TdBg),
    "tests" to FileTypeInfo("TESTS", Icons.Default.Assignment, TestAccent, TestBg),
    "resume" to FileTypeInfo("RÉSUMÉS", Icons.Default.Summarize, ResumeAccent, ResumeBg)
)

fun fileTypeAccent(extension: String, categoryType: String): Pair<ImageVector, Color> {
    return when (extension) {
        "pdf" -> Icons.Default.PictureAsPdf to ColorPdf
        "jpg", "jpeg", "png", "gif", "webp", "bmp" -> Icons.Default.Image to Secondary
        "doc", "docx" -> Icons.Default.Description to Primary
        "ppt", "pptx" -> Icons.Default.Slideshow to ColorZip
        "xls", "xlsx" -> Icons.Default.TableChart to ColorDoc
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.Folder to ColorZip
        "py", "java", "kt", "js", "ts", "cpp", "c", "h", "rs", "go" -> Icons.Default.Code to ColorCode
        "txt" -> Icons.Default.TextSnippet to ColorOther
        "mp4", "avi", "mkv", "mov" -> Icons.Default.Videocam to ColorCode
        "mp3", "wav", "flac" -> Icons.Default.Audiotrack to ColorZip
        else -> when (categoryType) {
            "course" -> Icons.Default.Description to Primary
            "exams" -> Icons.Default.Quiz to ExamAccent
            "resume" -> Icons.Default.Summarize to ResumeAccent
            else -> Icons.Default.InsertDriveFile to ColorOther
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: FilesViewModel,
    yearName: String,
    semester: Int,
    moduleName: String,
    onFileClick: (FileItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(yearName, semester, moduleName) {
        viewModel.loadModuleFiles(yearName, semester, moduleName)
    }

    val files by viewModel.files.collectAsState()
    val moduleTitle by viewModel.moduleTitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val downloadedUrls by viewModel.downloadedUrls.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val filteredFiles = remember(files, selectedCategory) {
        if (selectedCategory == null) files
        else files.filter { it.type == selectedCategory }
    }

    val groupedFiles = remember(filteredFiles) {
        filteredFiles.groupBy { it.type }.entries
            .sortedBy { (type, _) ->
                when (type) {
                    "course" -> 0; "exams" -> 1; "TD&TP" -> 2; "resume" -> 3; "tests" -> 4
                    else -> 5
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = moduleTitle.ifEmpty { "Chargement..." },
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextSecondary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { /* batch download */ }) {
                        Text(
                            text = "Tout télécharger",
                            color = Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background,
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            CategorySelector(
                selectedCategory = selectedCategory,
                files = files,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(5) {
                            ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp))
                        }
                    }
                }
            } else if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Aucun fichier dans cette catégorie",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groupedFiles.forEach { (type, typeFiles) ->
                        val info = fileTypeMap[type] ?: return@forEach

                        if (selectedCategory == null) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Background
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = info.label,
                                            color = TextLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(info.accent.copy(alpha = 0.18f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${typeFiles.size}",
                                                color = info.accent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        itemsIndexed(typeFiles) { index, file ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(tween(250 + index * 40)) +
                                        slideInVertically(tween(250 + index * 40)) { it / 4 }
                            ) {
                                FileRowCard(
                                    file = file,
                                    info = info,
                                    isBookmarked = bookmarkedIds.contains(file.id),
                                    isDownloaded = downloadedUrls.contains(file.url),
                                    onClick = { onFileClick(file) },
                                    onBookmarkToggle = { viewModel.toggleBookmark(file) },
                                    onDownloadToggle = {
                                        if (downloadedUrls.contains(file.url)) {
                                            viewModel.deleteDownload(file.url)
                                        } else {
                                            viewModel.downloadFile(file)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CategoryTab(
    val type: String?,
    val label: String,
    val accent: Color
)

private val categoryTabs = listOf(
    CategoryTab(null, "Tout", TextLabel),
    CategoryTab("course", "Cours", CourseAccent),
    CategoryTab("exams", "Examens", ExamAccent),
    CategoryTab("resume", "Résumé", ResumeAccent),
    CategoryTab("TD&TP", "TD & TP", TdAccent),
    CategoryTab("tests", "Tests", TestAccent)
)

@Composable
private fun CategorySelector(
    selectedCategory: String?,
    files: List<FileItem>,
    onCategorySelected: (String?) -> Unit
) {
    val categoryCounts = remember(files) {
        files.groupBy { it.type }.mapValues { it.value.size }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categoryTabs.forEach { tab ->
            val count = if (tab.type == null) files.size else categoryCounts[tab.type] ?: 0
            val isSelected = selectedCategory == tab.type

            Surface(
                onClick = { onCategorySelected(tab.type) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) tab.accent else Surface,
                tonalElevation = if (isSelected) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else tab.accent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$count",
                            color = if (isSelected) Color.White else tab.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileRowCard(
    file: FileItem,
    info: FileTypeInfo,
    isBookmarked: Boolean,
    isDownloaded: Boolean,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onDownloadToggle: () -> Unit
) {
    val extension = file.name.substringAfterLast('.', "").lowercase()
    val (icon, color) = fileTypeAccent(extension, file.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        IconBadge(
            icon = icon,
            accentColor = color,
            containerColor = SurfaceElevated
        )
        Spacer(modifier = Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = extension.uppercase(),
                        fontSize = 11.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDownloadToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Téléchargé",
                            tint = Success,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Télécharger",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Favori",
                        tint = if (isBookmarked) Primary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
