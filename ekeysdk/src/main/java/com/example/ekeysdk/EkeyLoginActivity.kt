package com.example.ekeysdk

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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            cancelAndFinish()
        }
        setContent {
            val context = LocalContext.current
            val controller = remember {
                EkeyWebViewController(context) { uri ->
                    Ekey.deliverResult(EkeyLoginResult.Completed(uri))
                    finish()
                }
            }

            LaunchedEffect(Unit) {
                controller.start()
            }

            LaunchedEffect(Unit) {
                EkeyLoginEvents.onResume.collect {
                    controller.resume()
                }
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
                    factory = { controller.webView },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }

    private fun cancelAndFinish() {
        Ekey.deliverResult(EkeyLoginResult.Cancelled)
        finish()
    }
}
