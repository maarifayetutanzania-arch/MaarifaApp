package com.maarifa.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.messaging.FirebaseMessaging
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.navigation.MaarifaNavGraph
import com.maarifa.app.ui.theme.MaarifaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaarifaTheme {
                val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                val container = maarifaContainer()
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    val uid = container.authRepository.currentUserId ?: return@LaunchedEffect
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        scope.launch { container.notificationRepository.saveFcmToken(uid, token) }
                    }
                }

                MaarifaNavGraph()
            }
        }
    }
}
