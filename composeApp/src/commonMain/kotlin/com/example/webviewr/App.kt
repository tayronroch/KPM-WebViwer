package com.example.webviewr

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * WebViewer App - Carrega sistemas web como um app nativo
 *
 * Para configurar a URL, edite o arquivo AppConfig.kt
 */
@Composable
@Preview
fun App() {
    // WebView preenchendo toda a tela
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        WebView(
            url = AppConfig.APP_URL,
            modifier = Modifier.fillMaxSize()
        )
    }
}