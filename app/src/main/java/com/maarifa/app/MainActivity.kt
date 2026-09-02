package com.maarifa.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.MaarifaNavGraph
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.AuthViewModelFactory
import com.maarifa.app.ui.theme.MaarifaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        val container = maarifaContainer()
        AuthViewModelFactory(container.authRepository, container.authService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Native Splash haitaondoka mpaka authViewModel imalize kuangalia session
        splashScreen.setKeepOnScreenCondition {
            authViewModel.state.value.checkingSession
        }

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase init error: ${e.message}")
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

                val container = maarifaContainer()
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
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

                MaarifaNavGraph(authViewModel = authViewModel)
            }
        }
    }
}
