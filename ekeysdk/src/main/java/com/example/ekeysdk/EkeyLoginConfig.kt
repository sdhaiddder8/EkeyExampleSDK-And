package com.example.ekeysdk

import android.net.Uri
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

internal data class EkeyAuthorizationRequest(
    val uri: Uri,
    val codeVerifier: String,
    val state: String
)

/**
 * All eKey 2.0 integration config lives here, inside the SDK. Consuming apps only ever call
 * [Ekey.initiateLogin] and [Ekey.handleIntent] / [Ekey.handleOpenURL] — nothing to configure
 * from the app side.
 */
internal object EkeyLoginConfig {
    const val AUTHORIZATION_BASE_URL = "https://login.ekey.bh/oidc/auth"
    const val CLIENT_ID = "ugiTHWoHreu2j8nACiBQV"
    const val REDIRECT_URI = "https://mobileapp.necremit.com/beyon/WS_MobileAPICALLS.asmx"
    const val SCOPE = "openid id-* id-*-additional id-*-photo ekyc-bhr-name ekyc-bhr-address " +
        "ekyc-bhr-birth ekyc-bhr-nationality ekyc-bhr-contact ekyc-bhr-employment " +
        "ekyc-bhr-passport ekyc-bhr-resident ekyc-bhr-photo ekyc-bhr-disability " +
        "liveness_proof liveness_photo"
    const val CUSTOM_URL_SCHEME = "necekey"
    const val FOCUS_URI = "$CUSTOM_URL_SCHEME://callback"

    // Production's App-to-App launch domain.
    val EKEY_APP_TO_APP_DOMAINS = listOf("app.ekey.bh")

    fun makeAuthorizationRequest(): EkeyAuthorizationRequest {
        val codeVerifier = randomUrlSafeString(32)
        val codeChallenge = sha256UrlSafe(codeVerifier)
        val state = randomUrlSafeString(16)

        val uri = Uri.parse(AUTHORIZATION_BASE_URL).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", SCOPE)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("response_mode", "query")
            .appendQueryParameter("prompt", "consent")
            .appendQueryParameter("focus_uri", FOCUS_URI)
            .build()

        return EkeyAuthorizationRequest(uri = uri, codeVerifier = codeVerifier, state = state)
    }

    private fun randomUrlSafeString(byteCount: Int): String {
        val bytes = ByteArray(byteCount)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun sha256UrlSafe(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
