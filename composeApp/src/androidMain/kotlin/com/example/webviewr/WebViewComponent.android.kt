package com.example.webviewr

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android implementation of WebView using android.webkit.WebView
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
    onBack: (() -> Boolean)?,
    onLoadingChange: ((Boolean) -> Unit)?
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // ============================================
                // CONFIGURAÇÕES BÁSICAS DE JAVASCRIPT
                // ============================================
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true

                // ============================================
                // STORAGE E DATABASE
                // ============================================
                settings.domStorageEnabled = true
                settings.databaseEnabled = true

                // ============================================
                // CONFIGURAÇÕES DE VIEWPORT E LAYOUT
                // ============================================
                // Configurar como navegador Chrome mobile padrão
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING

                // ============================================
                // ZOOM
                // ============================================
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                // ============================================
                // SEGURANÇA E CONTEÚDO MISTO
                // ============================================
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.allowFileAccessFromFileURLs = true
                settings.allowUniversalAccessFromFileURLs = true

                // ============================================
                // CACHE E PERFORMANCE
                // ============================================
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                // ============================================
                // MEDIA E GEOLOCALIZAÇÃO
                // ============================================
                settings.mediaPlaybackRequiresUserGesture = false
                settings.setGeolocationEnabled(true)

                // ============================================
                // USER AGENT - Simular navegador Chrome moderno
                // ============================================
                // User agent atualizado para compatibilidade com Next.js
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36"

                // ============================================
                // RENDERING E HARDWARE
                // ============================================
                // Usa aceleração de hardware para melhor performance
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                // Background branco para evitar problemas de transparência
                setBackgroundColor(android.graphics.Color.WHITE)

                // ============================================
                // CONFIGURAÇÕES ADICIONAIS DE COMPATIBILIDADE
                // ============================================
                settings.setSupportMultipleWindows(true)
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false
                settings.blockNetworkLoads = false

                // Configurações adicionais para melhor compatibilidade
                settings.safeBrowsingEnabled = true
                settings.offscreenPreRaster = true

                // Habilita recursos modernos do WebView
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    settings.safeBrowsingEnabled = true
                }

                // Force enable GPU rasterization
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    settings.offscreenPreRaster = true
                }

                // Desabilita force dark mode para evitar problemas de renderização
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    settings.isAlgorithmicDarkeningAllowed = false
                }

                // Set WebViewClient to handle navigation and errors
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: WebView?,
                        url: String?,
                        favicon: android.graphics.Bitmap?
                    ) {
                        super.onPageStarted(view, url, favicon)
                        Log.d("WebView", "Page started loading: $url")
                        onLoadingChange?.invoke(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("WebView", "Page finished loading: $url")

                        // Fix definitivo: forçar main a ter altura e exibir conteúdo
                        view?.evaluateJavascript("""
                            (function() {
                                // Esperar React terminar de renderizar
                                setTimeout(() => {
                                    const main = document.querySelector('main');
                                    if (main) {
                                        // Forçar main a ter altura maior (aumentei de 300px para 700px)
                                        main.style.minHeight = '1000px';
                                        main.style.maxHeight = '1000px';
                                        main.style.height = 'auto';
                                        main.style.flex = '1';
                                        main.style.display = 'flex';
                                        main.style.flexDirection = 'column';

                                        // Forçar primeiro filho do main a aparecer (aumentei de 200px para 600px)
                                        const firstChild = main.firstElementChild;
                                        if (firstChild) {
                                            firstChild.style.display = 'block';
                                            firstChild.style.minHeight = '600px';
                                            firstChild.style.flex = '1';
                                        }

                                        console.log('[FIX] Main height:', main.clientHeight, 'First child:', firstChild?.tagName);
                                    } else {
                                        console.error('[FIX] Main não encontrado!');
                                    }
                                }, 1000);
                            })();
                        """.trimIndent(), null)

                        onLoadingChange?.invoke(false)
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        super.onPageCommitVisible(view, url)
                        Log.d("WebView", "Page became visible: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        Log.e("WebViewError", "URL: ${request?.url}, Error: ${error?.description}, ErrorCode: ${error?.errorCode}")
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        // Permite navegação dentro do WebView
                        return false
                    }
                }

                // Set WebChromeClient for complete JavaScript support and console logging
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            // Log apenas erros críticos
                            if (it.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                Log.e(
                                    "WebViewJS",
                                    "[${it.sourceId()}:${it.lineNumber()}] ${it.message()}"
                                )
                            }
                        }
                        return true
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        Log.d("WebView", "Loading progress: $newProgress%")
                    }

                    // Handle permission requests from web content (camera, microphone, etc)
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.let {
                            Log.d("WebView", "Permission request: ${it.resources.joinToString()}")
                            // Grant all requested permissions
                            // Note: Android runtime permissions should be handled in MainActivity
                            it.grant(it.resources)
                        }
                    }

                    // Handle geolocation permission requests
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: GeolocationPermissions.Callback?
                    ) {
                        Log.d("WebView", "Geolocation permission request from: $origin")
                        callback?.invoke(origin, true, false)
                    }
                }

                // Load the URL
                Log.d("WebView", "Loading URL: $url")
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                Log.d("WebView", "Updating URL to: $url")
                webView.loadUrl(url)
            }
        }
    )

    // Handle back navigation
    DisposableEffect(onBack) {
        val callback = onBack
        onDispose { }
    }
}

/**
 * Extension function to check if WebView can go back
 * This can be used by the MainActivity to handle back button
 */
fun WebView.canNavigateBack(): Boolean = canGoBack()

/**
 * Extension function to navigate back in WebView
 */
fun WebView.navigateBack() {
    if (canGoBack()) {
        goBack()
    }
}
