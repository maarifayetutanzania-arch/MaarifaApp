package com.maarifa.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.AuthViewModelFactory
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.OtpVerificationScreen
import com.maarifa.app.ui.auth.RegisterScreen
import com.maarifa.app.ui.student.StudentHomeScreen
import com.maarifa.app.ui.teacher.TeacherHomeScreen
import com.maarifa.app.ui.teacher.TeacherVerificationPendingScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object OtpVerification : Screen("otp_verification/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_verification/$phoneNumber"
    }
    data object StudentHome : Screen("student_home")
    data object TeacherHome : Screen("teacher_home")
    data object TeacherPending : Screen("teacher_pending")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            maarifaContainer().authRepository,
            maarifaContainer().authService
        )
    )
) {
    val state by authViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            LaunchedEffect(state.checkingSession, state.isSignedIn, state.isEmailVerified, state.profile) {
                if (!state.checkingSession) {
                    val target = when {
                        state.isSignedIn && state.isEmailVerified && state.profile?.role == "TEACHER" -> {
                            // Kama status ya mwalimu ni VERIFIED/APPROVED, mpeleke TeacherHome
                            if (state.profile?.verificationStatus == "VERIFIED" || state.profile?.verificationStatus == "APPROVED") {
                                Screen.TeacherHome.route
                            } else {
                                Screen.TeacherPending.route
                            }
                        }
                        state.isSignedIn && state.isEmailVerified -> Screen.StudentHome.route
                        state.isSignedIn && !state.isEmailVerified -> Screen.Register.route
                        else -> Screen.Login.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // 2. Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 3. Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = {
                    val userRole = state.profile?.role ?: "STUDENT"
                    val target = if (userRole == "TEACHER") Screen.TeacherPending.route else Screen.StudentHome.route

                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToOtp = { phone ->
                    navController.navigate(Screen.OtpVerification.createRoute(phone))
                },
                authViewModel = authViewModel
            )
        }

        // 4. OTP Verification Screen
        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber").orEmpty()
            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                onVerificationSuccess = {
                    val userRole = state.profile?.role ?: "STUDENT"
                    val target = if (userRole == "TEACHER") Screen.TeacherPending.route else Screen.StudentHome.route
                    
                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // 5. Student Home
        composable(Screen.StudentHome.route) {
            StudentHomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 6. Teacher Home
        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 7. Teacher Verification Pending Screen
        composable(Screen.TeacherPending.route) {
            TeacherVerificationPendingScreen(
                onVerified = {
                    navController.navigate(Screen.TeacherHome.route) {
                        popUpTo(Screen.TeacherPending.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
