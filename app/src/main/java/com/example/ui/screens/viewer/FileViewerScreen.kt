package com.example.ui.screens.viewer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FileViewerScreen(
    viewModel: ViewerViewModel,
    url: String,
    fileName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isBookmarked by remember { mutableStateOf(false) }
    val localPath by viewModel.localPath.collectAsState()

    LaunchedEffect(url) {
        viewModel.checkLocalDownload(url)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = fileName,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp,
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
                        IconButton(onClick = { /* Share */ }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Partager",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        val downloadIcon = if (localPath != null) Icons.Default.CheckCircle else Icons.Default.Download
                        val downloadTint = if (localPath != null) Success else TextSecondary
                        IconButton(onClick = { /* Download */ }) {
                            Icon(
                                imageVector = downloadIcon,
                                contentDescription = "Télécharger",
                                tint = downloadTint,
                                modifier = Modifier.size(20.dp)
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
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    progress = newProgress / 100f
                                }
                            }

                            val loadUrl = localPath?.let { "file:///$it" } ?: url
                            loadUrl(loadUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isLoading && progress < 1f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = Primary,
                        trackColor = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .align(Alignment.TopCenter)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = SurfaceElevated,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            IconButton(onClick = { /* Share */ }) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Partager",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = { isBookmarked = !isBookmarked }) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Favori",
                                    tint = if (isBookmarked) Primary else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = { /* Download */ }) {
                                Icon(
                                    imageVector = if (localPath != null) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = "Télécharger",
                                    tint = if (localPath != null) Success else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
