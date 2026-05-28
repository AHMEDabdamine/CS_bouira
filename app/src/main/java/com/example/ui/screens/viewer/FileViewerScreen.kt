package com.example.ui.screens.viewer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.settings.SettingsManager
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FileViewerScreen(
    viewModel: ViewerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val showNavButtons by settingsManager.showNavButtons.collectAsState(initial = true)
    val enablePinchZoom by settingsManager.enablePinchZoom.collectAsState(initial = true)

    val fileList by viewModel.fileList.collectAsState()
    val currentIndex by viewModel.currentFileIndex.collectAsState()
    val currentFile = viewModel.currentFile

    var webProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    val localPath by viewModel.localPath.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val url = currentFile?.url ?: ""
    val fileName = currentFile?.name ?: ""

    LaunchedEffect(url) {
        if (url.isNotEmpty()) {
            viewModel.checkLocalDownload(url)
        }
    }

    val extension = fileName.substringAfterLast('.', "").lowercase()
    val isLocallyAvailable = localPath != null
    val isImage = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    val isText = extension in listOf("txt", "md", "csv", "xml", "json", "html", "css", "js", "kt", "java", "py", "cpp", "c", "h")
    val isPdf = extension == "pdf"

    val hasMultipleFiles = fileList.size > 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = fileName,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (hasMultipleFiles && showNavButtons) {
                                Text(
                                    text = stringResource(R.string.file_index, currentIndex + 1, fileList.size),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceElevated
                    )
                )
            },
            containerColor = Background,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                        } else {
                            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                        }
                    },
                    label = "fileContentTransition",
                    modifier = Modifier.fillMaxSize()
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            isLocallyAvailable && isImage -> {
                                ZoomableImage(
                                    localPath = localPath!!,
                                    fileName = fileName,
                                    enabled = enablePinchZoom
                                )
                            }
                            isLocallyAvailable && isText -> {
                                TextFileContentView(localPath = localPath!!)
                            }
                            isLocallyAvailable && isPdf -> {
                                PdfViewerWithFallback(localPath = localPath!!)
                            }
                            isLocallyAvailable -> {
                                LocalFileWebView(localPath = localPath!!)
                            }
                            else -> {
                                OnlineWebView(
                                    url = url,
                                    onProgressChanged = { webProgress = it },
                                    onLoadingChanged = { isLoading = it }
                                )

                                if (isLoading && webProgress < 1f) {
                                    LinearProgressIndicator(
                                        progress = { webProgress },
                                        color = Primary,
                                        trackColor = Color.Transparent,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .align(Alignment.TopCenter)
                                    )
                                }
                            }
                        }
                    }
                }

                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        color = Primary,
                        trackColor = Color.Transparent.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }

        if (hasMultipleFiles && showNavButtons) {
            if (viewModel.hasPrevious) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectTapGestures { viewModel.goToPrevious() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = stringResource(R.string.previous_file),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (viewModel.hasNext) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectTapGestures { viewModel.goToNext() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = stringResource(R.string.next_file),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    localPath: String,
    fileName: String,
    enabled: Boolean
) {
    val state = remember { ZoomState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (enabled) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        state.zoom = (state.zoom * zoom).coerceIn(1f, 5f)
                        if (state.zoom > 1f) {
                            state.offsetX += pan.x
                            state.offsetY += pan.y
                        } else {
                            state.offsetX = 0f
                            state.offsetY = 0f
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = state.zoom
                scaleY = state.zoom
                translationX = state.offsetX
                translationY = state.offsetY
            }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(File(localPath))
                .crossfade(true)
                .build(),
            contentDescription = fileName,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )

        if (enabled && state.zoom > 1f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${(state.zoom * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private class ZoomState {
    var zoom by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
}

@Composable
private fun PdfViewerWithFallback(localPath: String) {
    var pages by remember { mutableStateOf<List<Bitmap>?>(null) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(localPath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(localPath)
                if (!file.exists()) { error = true; return@withContext }
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                val pageList = mutableListOf<Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    pageList.add(bitmap)
                    page.close()
                }
                renderer.close()
                fd.close()
                pages = pageList
            } catch (e: Exception) {
                error = true
            }
        }
    }

    val pageBitmaps = pages
    if (error) {
        LocalFileWebView(localPath = localPath)
    } else if (pageBitmaps == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    } else {
        ZoomablePdfViewer(pages = pageBitmaps)
    }
}

@Composable
private fun ZoomablePdfViewer(pages: List<Bitmap>) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var contentWidth by remember { mutableIntStateOf(0) }
    var contentHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    if (newScale != scale) {
                        val ratio = newScale / scale
                        offsetX = (centroid.x - ratio * (centroid.x - offsetX))
                        offsetY = (centroid.y - ratio * (centroid.y - offsetY))
                        scale = newScale
                    } else if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetY = (offsetY + pan.y).coerceIn(
                            -(contentHeight - contentHeight / scale).coerceAtLeast(0f),
                            0f
                        )
                    }
                    offsetX = offsetX.coerceIn(
                        -(contentWidth * scale - contentWidth).coerceAtLeast(0f),
                        (contentWidth * scale - contentWidth).coerceAtLeast(0f)
                    )
                    offsetY = offsetY.coerceIn(
                        -(contentHeight * scale - contentHeight / scale).coerceAtLeast(0f),
                        (contentHeight * scale - contentHeight / scale).coerceAtLeast(0f)
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.5f) {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        } else {
                            scale = 3f
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .onSizeChanged { contentWidth = it.width; contentHeight = it.height }
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
        ) {
            Column {
                pages.forEachIndexed { index, bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.page_format, index + 1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        if (scale > 1f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LocalFileWebView(localPath: String) {
    val context = LocalContext.current
    val contentUri = remember(localPath) {
        try {
            val file = File(localPath)
            if (file.exists()) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else null
        } catch (_: Exception) { null }
    }

    if (contentUri == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.file_not_found), color = TextSecondary, fontSize = 16.sp)
            }
        }
    } else {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    loadUrl(contentUri.toString())
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OnlineWebView(
    url: String,
    onProgressChanged: (Float) -> Unit,
    onLoadingChanged: (Boolean) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress / 100f)
                    }
                }

                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun TextFileContentView(localPath: String) {
    var content by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(localPath) {
        withContext(Dispatchers.IO) {
            content = try {
                File(localPath).readText()
            } catch (_: Exception) { null }
        }
    }

    val text = content
    if (text == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.cannot_read_file), color = TextSecondary, fontSize = 16.sp)
        }
    } else {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceElevated)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}
