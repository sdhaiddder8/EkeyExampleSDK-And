package com.example.ekeyexamplesdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ekeyexamplesdk.ui.theme.EkeyExampleSDKTheme

private const val TAG = "Ekey"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EkeyExampleSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val data = intent.data
        if (data != null && data.scheme == EkeyLoginConfig.CUSTOM_URL_SCHEME) {
            Log.d(TAG, "focus_uri received: $data")
            EkeyLoginEvents.notifyResume()
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var showingEkeyLogin by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFE8F0FE))
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🌐", fontSize = 40.sp)
        Text(
            text = "Hello, NEC testing Flow!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )
        Button(
            onClick = {
                Log.d(TAG, "Initiate Ekey Flow tapped")
                showingEkeyLogin = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF007AFF), Color(0xFF5856D6))
                    )
                )
        ) {
            Text(
                text = "Initiate Ekey Flow",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 14.dp)
            )
        }
    }

    if (showingEkeyLogin) {
        EkeyLoginScreen(
            onDismiss = { showingEkeyLogin = false },
            onCompleted = { uri ->
                Log.d(TAG, "Ekey login completed: $uri")
                showingEkeyLogin = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EkeyExampleSDKTheme {
        HomeScreen()
    }
}
