package com.example.ui.screens.viewer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CosmicDark

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
    val localPath by viewModel.localPath.collectAsState()

    LaunchedEffect(url) {
        viewModel.checkLocalDownload(url)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicDark)
            .testTag("file_viewer_root")
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
            modifier = Modifier.fillMaxSize().testTag("webview")
        )

        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress },
                color = AccentGreen,
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
            )
        }

        Surface(
            color = CosmicDark.copy(alpha = 0.8f),
            modifier = Modifier
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .align(Alignment.TopStart),
            tonalElevation = 6.dp
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("viewer_close_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return",
                    tint = AccentGreen
                )
            }
        }
    }
}
