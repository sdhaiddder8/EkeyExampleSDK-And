package com.example.ekeysdk

import android.app.Activity
import android.content.Intent

/**
 * Entry point for the eKey 2.0 app-to-app login SDK. All integration config (client_id,
 * redirect_uri, scope, endpoints, custom URL scheme) lives inside the SDK, including the
 * `necekey://callback` intent handling (registered on EkeyLoginActivity) — the app only calls
 * this one entry point.
 *
 * Usage:
 * ```kotlin
 * Ekey.initiateLogin(this) { result ->
 *     when (result) {
 *         is EkeyLoginResult.Completed -> {
 *             // Send result.redirectUri's `code` + `state` to your back-end for token exchange.
 *         }
 *         EkeyLoginResult.Cancelled -> {}
 *         is EkeyLoginResult.Failed -> {
 *             // e.g. EkeyLoginError.StateMismatch — reject the login, do not treat as success.
 *         }
 *     }
 * }
 * ```
 */
object Ekey {
    private var pendingCallback: ((EkeyLoginResult) -> Unit)? = null

    /** Launches the eKey login flow from `activity`. */
    fun initiateLogin(activity: Activity, onResult: (EkeyLoginResult) -> Unit) {
        pendingCallback = onResult
        activity.startActivity(Intent(activity, EkeyLoginActivity::class.java))
    }

    internal fun deliverResult(result: EkeyLoginResult) {
        pendingCallback?.invoke(result)
        pendingCallback = null
    }
}
