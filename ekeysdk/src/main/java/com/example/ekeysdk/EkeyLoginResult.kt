package com.example.ekeysdk

import android.net.Uri

sealed class EkeyLoginResult {
    /**
     * The WebView reached the configured redirect_uri, its `state` matched the one this SDK
     * generated for the request, and the SDK successfully exchanged the code for tokens.
     * [redirectUri] carries the `code`/`state` query parameters, [codeVerifier] is the PKCE
     * verifier generated for this same request, and [identity] carries the decoded ID token
     * claims plus KYC data (if granted) — the SDK performs the token exchange internally rather
     * than requiring a back-end round trip. See EkeyTokenExchange.kt for the security trade-off
     * this implies (client_secret ships inside the app binary).
     */
    data class Completed(
        val redirectUri: Uri,
        val codeVerifier: String,
        val identity: EkeyIdentityData
    ) : EkeyLoginResult()

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

    /** The code-for-token exchange or KYC data fetch failed. See [EkeyTokenExchangeError]. */
    data class TokenExchangeFailed(val error: EkeyTokenExchangeError) : EkeyLoginError()
}
