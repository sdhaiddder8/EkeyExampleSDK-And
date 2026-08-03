package com.example.ekeyexamplesdk

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkeyLoginScreen(onDismiss: () -> Unit, onCompleted: (Uri) -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val context = LocalContext.current
        val controller = remember {
            EkeyWebViewController(context) { url ->
                onCompleted(url)
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
                        IconButton(onClick = onDismiss) {
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
