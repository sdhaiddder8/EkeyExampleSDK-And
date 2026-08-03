package com.example.ekeysdk

import android.app.Activity
import android.content.Intent
import android.net.Uri

/**
 * Entry point for the eKey 2.0 app-to-app login SDK. All integration config (client_id,
 * redirect_uri, scope, endpoints, custom URL scheme) lives inside the SDK — the app only
 * calls these entry points.
 *
 * Usage:
 * ```kotlin
 * // Forward every onNewIntent call (app's Activity must have android:launchMode="singleTop"
 * // and an intent-filter for the necekey scheme):
 * override fun onNewIntent(intent: Intent) {
 *     super.onNewIntent(intent)
 *     Ekey.handleIntent(intent)
 * }
 *
 * // Start the flow from any Activity:
 * Ekey.initiateLogin(this) { result ->
 *     when (result) {
 *         is EkeyLoginResult.Completed -> {
 *             // Send result.redirectUri's `code` + `state` to your back-end for token exchange.
 *         }
 *         EkeyLoginResult.Cancelled -> {}
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

    /**
     * Forward every `onNewIntent(Intent)` call here. Returns true if the intent's data was this
     * SDK's `focus_uri` callback and was handled.
     */
    fun handleIntent(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        return handleOpenURL(uri)
    }

    /**
     * Returns true if `uri`'s scheme was this SDK's `focus_uri` callback and was handled.
     */
    fun handleOpenURL(uri: Uri): Boolean {
        if (uri.scheme != EkeyLoginConfig.CUSTOM_URL_SCHEME) {
            return false
        }
        EkeyLoginEvents.notifyResume()
        return true
    }

    internal fun deliverResult(result: EkeyLoginResult) {
        pendingCallback?.invoke(result)
        pendingCallback = null
    }
}
