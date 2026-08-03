package com.example.ekeyexamplesdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

private const val TAG = "Ekey"

class EkeyWebViewController(context: Context, private val onCompleted: (Uri) -> Unit) {
    val webView: WebView = WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(true)
    }

    private var didOpenEkeyApp = false

    init {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                val frameKind = if (request.isForMainFrame) "main" else "subframe"
                Log.d(TAG, "NAV[$frameKind]: $url")
                return intercept(url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                Log.d(TAG, "didFinish: $url")
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                Log.d(TAG, "didFail: ${error.description}")
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            /**
             * The eKey login page opens `mobileLogin` via `window.open()`/`target="_blank"`
             * rather than a same-frame navigation, so it routes through onCreateWindow instead
             * of shouldOverrideUrlLoading. We spin up a throwaway WebView just to capture the
             * target URL from its first navigation request, then hand it to intercept()
             * ourselves and discard it — Android has no way to read the URL from onCreateWindow
             * directly.
             */
            override fun onCreateWindow(
                view: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                val throwaway = WebView(view.context)
                throwaway.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        popupView: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        Log.d(TAG, "POPUP: ${request.url}")
                        intercept(request.url)
                        return true
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = throwaway
                resultMsg.sendToTarget()
                return true
            }
        }
    }

    fun start() {
        didOpenEkeyApp = false
        val request = EkeyLoginConfig.makeAuthorizationRequest()
        Log.d(TAG, "PKCE code_verifier (needed for backend token exchange): ${request.codeVerifier}")
        webView.loadUrl(request.uri.toString())
    }

    /**
     * Called when the app resumes via the `focus_uri` custom scheme. The WebView instance
     * (and its session cookies) is kept alive the whole time, so reloading it lets the
     * server-side OIDC interaction continue from consented-login through to redirect_uri,
     * instead of losing the session the way a torn-down WebView would.
     */
    fun resume() {
        webView.reload()
    }

    /** Returns true if the URL was handed off and should not be loaded by the WebView itself. */
    private fun intercept(url: Uri): Boolean {
        val urlString = url.toString()

        if (urlString.startsWith(EkeyLoginConfig.REDIRECT_URI)) {
            onCompleted(url)
            return true
        }

        if (!didOpenEkeyApp && EkeyLoginConfig.EKEY_APP_TO_APP_DOMAINS.any { urlString.contains(it) }) {
            didOpenEkeyApp = true
            val opened = try {
                webView.context.startActivity(Intent(Intent.ACTION_VIEW, url))
                true
            } catch (e: Exception) {
                false
            }
            Log.d(TAG, "app-to-app handoff opened: $opened — $urlString")
            return true
        }

        return false
    }
}
