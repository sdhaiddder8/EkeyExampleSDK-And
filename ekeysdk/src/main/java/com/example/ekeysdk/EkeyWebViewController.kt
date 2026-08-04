package com.example.ekeysdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

internal class EkeyWebViewController(
    context: Context,
    private val onCompleted: (Uri) -> Unit,
    private val onFailed: (EkeyLoginError) -> Unit
) {
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
    private var expectedState: String? = null

    init {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return intercept(request.url)
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
        expectedState = request.state
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
            if (stateMatches(url)) {
                onCompleted(url)
            } else {
                onFailed(EkeyLoginError.StateMismatch)
            }
            return true
        }

        if (!didOpenEkeyApp && EkeyLoginConfig.EKEY_APP_TO_APP_DOMAINS.any { urlString.contains(it) }) {
            didOpenEkeyApp = true
            try {
                webView.context.startActivity(Intent(Intent.ACTION_VIEW, url))
            } catch (e: Exception) {
                // No app/browser available to handle the URL; nothing further to do.
            }
            return true
        }

        return false
    }

    /**
     * Guards against CSRF/session-mixup: redirect_uri's `state` must match the one generated for
     * this specific authorization request.
     */
    private fun stateMatches(url: Uri): Boolean {
        val expected = expectedState ?: return false
        return url.getQueryParameter("state") == expected
    }
}
