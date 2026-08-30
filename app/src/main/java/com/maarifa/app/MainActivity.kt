package com.maarifa.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.maarifa.app.navigation.MaarifaNavGraph
import com.maarifa.app.ui.theme.MaarifaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase init error: ${e.message}")
        }

        setContent {
            MaarifaTheme {
                var crashError by remember { mutableStateOf<String?>(null) }

                if (crashError != null) {
                    // Kama kuna kosa, badala ya kujifunga itaonyesha kosa hapa kwenye kioo!
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "CRASH DETECTED:\n\n$crashError", color = Color.Red)
                    }
                } else {
                    try {
                        // Kizuizi cha kukamata crash zote za startup
                        MaarifaNavGraph()
                    } catch (e: Throwable) {
                        crashError = e.stackTraceToString()
                    }
                }
            }
        }
    }
}
