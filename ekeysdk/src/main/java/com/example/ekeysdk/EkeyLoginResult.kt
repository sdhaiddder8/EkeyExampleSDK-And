package com.example.ekeysdk

import android.net.Uri

sealed class EkeyLoginResult {
    /**
     * The WebView reached the configured redirect_uri. [redirectUri] carries the `code`/`state`
     * query parameters — exchange them for tokens from your back-end (integration guide §2.2.5).
     * This SDK never performs the token exchange itself, since that requires the client_secret,
     * which must not ship inside a mobile app.
     */
    data class Completed(val redirectUri: Uri) : EkeyLoginResult()

    /** The user dismissed the login screen before completing the flow. */
    object Cancelled : EkeyLoginResult()
}
