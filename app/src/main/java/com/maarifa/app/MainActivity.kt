package com.maarifa.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.MaarifaNavGraph
import com.maarifa.app.ui.theme.MaarifaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize Firebase App
        val isFirebaseInitialized = try {
            FirebaseApp.initializeApp(this)
            true
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase init error: ${e.message}")
            false
        }

        // 2. Initialize Container ONCE outside Compose tree
        val container = try {
            maarifaContainer()
        } catch (e: Exception) {
            Log.e("MainActivity", "Container init error: ${e.message}")
            null
        }

        setContent {
            MaarifaTheme {
                val notificationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val scope = rememberCoroutineScope()
                
                LaunchedEffect(Unit) {
                    if (isFirebaseInitialized && container != null) {
                        val uid = container.authRepository.currentUserId
                        if (uid != null) {
                            try {
                                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                    scope.launch { 
                                        try {
                                            container.notificationRepository.saveFcmToken(uid, token) 
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Error saving FCM token: ${e.message}")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Error getting FCM token: ${e.message}")
                            }
                        }
                    }
                }

                MaarifaNavGraph()
            }
        }
    }
}
