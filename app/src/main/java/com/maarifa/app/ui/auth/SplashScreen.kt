package com.maarifa.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.navigation.Routes
import com.maarifa.app.ui.theme.MaarifaForestDeep

@Composable
fun SplashScreen(authViewModel: AuthViewModel, navController: NavController) {
    val state by authViewModel.state.collectAsState()

    LaunchedEffect(state.checkingSession, state.isSignedIn, state.profile) {
        if (state.checkingSession) return@LaunchedEffect

        val profile = state.profile
        val destination = when {
            !state.isSignedIn -> Routes.WELCOME
            profile == null -> Routes.REGISTER
            profile.roleEnum == UserRole.TEACHER -> Routes.TEACHER_HOME
            else -> Routes.STUDENT_HOME
        }

        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundBrush = remember(primaryColor) {
        Brush.radialGradient(
            colors = listOf(primaryColor, MaarifaForestDeep),
            center = Offset(0.3f, 0.15f),
            radius = 1400f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Maarifa Logo",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = "Maarifa 2026",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Text(
                text = "Learn. Teach. Grow.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(28.dp)
            )
        }
    }
}
