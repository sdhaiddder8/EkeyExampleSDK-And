package com.example.ekeysdk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

internal class EkeyLoginActivity : ComponentActivity() {
    private var controller: EkeyWebViewController? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            cancelAndFinish()
        }
        setContent {
            val context = LocalContext.current
            val webController = remember {
                EkeyWebViewController(
                    context = context,
                    onCompleted = { uri ->
                        Ekey.deliverResult(EkeyLoginResult.Completed(uri))
                        finish()
                    },
                    onFailed = { error ->
                        Ekey.deliverResult(EkeyLoginResult.Failed(error))
                        finish()
                    }
                ).also { controller = it }
            }

            LaunchedEffect(Unit) {
                webController.start()
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Ekey Login") },
                        navigationIcon = {
                            IconButton(onClick = ::cancelAndFinish) {
                                Text("×")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                AndroidView(
                    factory = { webController.webView },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }

    /**
     * Called when eKey redirects back via `necekey://callback` (registered directly on this
     * Activity — see AndroidManifest.xml for why). The WebView (and its session cookies) has
     * been kept alive the whole time, so resuming it here lets the server-side OIDC interaction
     * continue from consented-login through to redirect_uri.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.data?.scheme == EkeyLoginConfig.CUSTOM_URL_SCHEME) {
            controller?.resume()
        }
    }

    private fun cancelAndFinish() {
        Ekey.deliverResult(EkeyLoginResult.Cancelled)
        finish()
    }
}
