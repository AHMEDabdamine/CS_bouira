package com.example.ui.screens.files

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.theme.*

data class CategoryTab(
    val id: String,
    val label: String,
    val icon: ImageVector
)

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

    val categories = listOf(
        CategoryTab("course", "Courses", Icons.Default.Description),
        CategoryTab("exams", "Exams", Icons.Default.Quiz),
        CategoryTab("resume", "Synthèse", Icons.Default.Summarize),
        CategoryTab("TD&TP", "TD & TP", Icons.AutoMirrored.Filled.Assignment),
        CategoryTab("tests", "Tests", Icons.AutoMirrored.Filled.Grading)
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentCategory = categories.getOrElse(selectedTabIndex) { categories[0] }

    val filteredFiles = remember(files, currentCategory) {
        files.filter { it.type.equals(currentCategory.id, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = moduleTitle.ifEmpty { "Loading..." },
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Course Resources & Archive",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("files_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicDark
                )
            )
        },
        containerColor = CosmicDark,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CosmicDark)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CosmicDark,
                contentColor = AccentGreen,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = AccentGreen
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("files_category_tab_row")
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTabIndex == index) AccentGreen else TextSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.label,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTabIndex == index) AccentGreen else TextSecondary
                                )
                            }
                        },
                        modifier = Modifier.testTag("category_tab_${cat.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            } else if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = currentCategory.icon,
                            contentDescription = "Empty",
                            tint = BorderDark,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No files found in this category",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Check other categories for available resources.",
                            color = BorderDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    modifier = Modifier.weight(1f).testTag("files_list")
                ) {
                    items(filteredFiles) { file ->
                        val isBookmarked = bookmarkedIds.contains(file.id)
                        val isDownloaded = downloadedUrls.contains(file.url)
                        FileCard(
                            file = file,
                            isBookmarked = isBookmarked,
                            isDownloaded = isDownloaded,
                            onFileClick = { onFileClick(file) },
                            onBookmarkToggle = { viewModel.toggleBookmark(file) },
                            onDownload = { viewModel.downloadFile(file) },
                            onDeleteDownload = { viewModel.deleteDownload(file.url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileCard(
    file: FileItem,
    isBookmarked: Boolean,
    isDownloaded: Boolean,
    onFileClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extension = file.name.substringAfterLast('.', "").lowercase()
    val (icon, color) = fileTypeIcon(extension, file.type)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onFileClick)
            .testTag("file_card_${file.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (extension.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = extension.uppercase(),
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = { if (isDownloaded) onDeleteDownload() else onDownload() },
                modifier = Modifier.size(32.dp).testTag("download_toggle_${file.id}")
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                    contentDescription = if (isDownloaded) "Downloaded" else "Download",
                    tint = if (isDownloaded) AccentGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onBookmarkToggle,
                modifier = Modifier.size(32.dp).testTag("bookmark_toggle_${file.id}")
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) AccentGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun fileTypeIcon(extension: String, categoryType: String): Pair<ImageVector, Color> {
    return when (extension) {
        "pdf" -> Icons.Default.PictureAsPdf to ColorPdf
        "jpg", "jpeg", "png", "gif", "webp", "bmp" -> Icons.Default.Image to ColorImage
        "doc", "docx" -> Icons.Default.Description to ColorDoc
        "ppt", "pptx" -> Icons.Default.Description to ColorZip
        "xls", "xlsx" -> Icons.Default.TableChart to ColorDoc
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.Folder to ColorZip
        "py", "java", "kt", "js", "ts", "cpp", "c", "h", "rs", "go" -> Icons.Default.Code to ColorCode
        "txt" -> Icons.AutoMirrored.Filled.TextSnippet to ColorOther
        "mp4", "avi", "mkv", "mov" -> Icons.Default.Videocam to ColorImage
        "mp3", "wav", "flac" -> Icons.Default.Audiotrack to ColorZip
        else -> when (categoryType) {
            "course" -> Icons.Default.Description to ColorDoc
            "exams" -> Icons.Default.Quiz to ColorZip
            "resume" -> Icons.Default.Summarize to ColorDoc
            else -> Icons.AutoMirrored.Filled.InsertDriveFile to ColorOther
        }
    }
}
