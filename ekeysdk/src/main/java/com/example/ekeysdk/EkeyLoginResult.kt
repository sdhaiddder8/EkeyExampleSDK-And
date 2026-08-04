package com.example.ekeysdk

import android.net.Uri

sealed class EkeyLoginResult {
    /**
     * The WebView reached the configured redirect_uri and its `state` matched the one this SDK
     * generated for the request. [redirectUri] carries the `code`/`state` query parameters —
     * exchange them for tokens from your back-end (integration guide §2.2.5). This SDK never
     * performs the token exchange itself, since that requires the client_secret, which must not
     * ship inside a mobile app.
     */
    data class Completed(val redirectUri: Uri) : EkeyLoginResult()

    /** The user dismissed the login screen before completing the flow. */
    object Cancelled : EkeyLoginResult()

    /** The flow reached redirect_uri but failed validation and was not treated as a successful login. */
    data class Failed(val error: EkeyLoginError) : EkeyLoginResult()
}

sealed class EkeyLoginError {
    /**
     * redirect_uri's `state` parameter was missing or didn't match the `state` this SDK
     * generated for the original authorization request — a possible CSRF/session-mixup, so the
     * result is rejected rather than treated as a successful login.
     */
    object StateMismatch : EkeyLoginError()
}
