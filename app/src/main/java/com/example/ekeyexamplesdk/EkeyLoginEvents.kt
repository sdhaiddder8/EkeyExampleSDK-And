package com.example.ekeyexamplesdk

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Notifies an active EkeyLoginScreen to resume its WebView when the app is reopened via the
 * `necekey://callback` focus_uri scheme, delivered through MainActivity.onNewIntent.
 */
object EkeyLoginEvents {
    private val _onResume = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onResume = _onResume.asSharedFlow()

    fun notifyResume() {
        _onResume.tryEmit(Unit)
    }
}
