package com.example.ekeysdk

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Notifies the active EkeyLoginActivity to resume its WebView when the app is reopened via the
 * `necekey://callback` focus_uri scheme, delivered through Ekey.handleIntent/handleOpenURL.
 */
internal object EkeyLoginEvents {
    private val _onResume = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onResume = _onResume.asSharedFlow()

    fun notifyResume() {
        _onResume.tryEmit(Unit)
    }
}
