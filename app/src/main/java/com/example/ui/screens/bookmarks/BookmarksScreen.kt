package com.example.ui.screens.bookmarks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.components.IconBadge
import com.example.ui.components.SectionLabel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onFileClick: (FileItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarkedFiles by viewModel.bookmarkedFiles.collectAsState()
    val downloadedUrls by viewModel.downloadedUrls.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favoris",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background,
        modifier = modifier
    ) { innerPadding ->
        if (bookmarkedFiles.isEmpty() && downloadedUrls.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Aucun élément",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ajoutez des documents à vos favoris\nou téléchargez-les pour y accéder rapidement.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = OnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Parcourir les modules",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Favorites section
                if (bookmarkedFiles.isNotEmpty()) {
                    item {
                        SectionLabel(text = "FAVORIS")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(
                        items = bookmarkedFiles,
                        key = { it.id }
                    ) { file ->
                        var visible by remember { mutableStateOf(true) }
                        if (visible) {
                            SwipeToDismissBox(
                                state = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.toggleBookmark(file)
                                            visible = false
                                            true
                                        } else false
                                    }
                                ),
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Error.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Supprimer",
                                            tint = Error,
                                            modifier = Modifier.padding(end = 20.dp)
                                        )
                                    }
                                },
                                enableDismissFromStartToEnd = false
                            ) {
                                BookmarkRowCard(
                                    file = file,
                                    onClick = { onFileClick(file) },
                                    onRemove = { viewModel.toggleBookmark(file) }
                                )
                            }
                        }
                    }
                }

                // Downloads section
                if (downloadedUrls.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionLabel(text = "TÉLÉCHARGEMENTS")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(
                        items = downloadedUrls.toList(),
                        key = { it }
                    ) { url ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconBadge(
                                icon = Icons.Default.Download,
                                accentColor = Success,
                                containerColor = SuccessBg
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fichier téléchargé",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = url.substringAfterLast("/"),
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.deleteDownload(url) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkRowCard(
    file: FileItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val extension = file.name.substringAfterLast('.', "").lowercase()
    val (icon, accentColor) = bookmarkFileInfo(extension, file.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = icon,
            accentColor = accentColor,
            containerColor = SurfaceElevated
        )
        Spacer(modifier = Modifier.width(14.dp))
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
                text = "dernièrement ajouté",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Retirer",
                tint = Error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private data class BookmarkFileInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

private fun bookmarkFileInfo(extension: String, categoryType: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (extension) {
        "pdf" -> Icons.Default.PictureAsPdf to ColorPdf
        "jpg", "jpeg", "png", "gif", "webp", "bmp" -> Icons.Default.Image to Secondary
        "doc", "docx" -> Icons.Default.Description to Primary
        "ppt", "pptx" -> Icons.Default.Slideshow to ColorZip
        "xls", "xlsx" -> Icons.Default.TableChart to ColorDoc
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.Folder to ColorZip
        "py", "java", "kt", "js", "ts", "cpp", "c", "h", "rs", "go" -> Icons.Default.Code to ColorCode
        "txt" -> Icons.Default.TextSnippet to ColorOther
        else -> when (categoryType) {
            "course" -> Icons.Default.Description to Primary
            "exams" -> Icons.Default.Quiz to ExamAccent
            "resume" -> Icons.Default.Summarize to ResumeAccent
            else -> Icons.Default.InsertDriveFile to ColorOther
        }
    }
}
