package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.OtpVerificationScreen
import com.maarifa.app.ui.auth.RegisterScreen
import com.maarifa.app.ui.auth.SplashScreen
import com.maarifa.app.ui.auth.WelcomeScreen
import com.maarifa.app.ui.student.StudentHomeScreen
import com.maarifa.app.ui.teacher.TeacherHomeScreen
import com.maarifa.app.ui.teacher.TeacherVerificationPendingScreen

@Composable
fun MaarifaNavGraph() {
    val navController = rememberNavController()
    val container = maarifaContainer()

    val authViewModel: AuthViewModel = viewModel(
        factory = SimpleViewModelFactory { AuthViewModel(container.authRepository, container.authService) }
    )

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen(authViewModel, navController) }
        composable(Routes.WELCOME) { WelcomeScreen(navController) }
        
        composable(Routes.LOGIN) { LoginScreen(authViewModel, navController) }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = {
                    navController.navigate(Routes.STUDENT_HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.OTP,
            arguments = listOf(navArgument("verificationId") { type = NavType.StringType })
        ) { entry ->
            val verificationId = entry.arguments?.getString("verificationId").orEmpty()
            OtpVerificationScreen(authViewModel, verificationId)
        }

        composable(Routes.STUDENT_HOME) {
            StudentHomeScreen(onSignedOut = {
                navController.navigate(Routes.WELCOME) { popUpTo(0) }
            })
        }

        composable(Routes.TEACHER_HOME) {
            TeacherHomeScreen(onSignedOut = {
                navController.navigate(Routes.WELCOME) { popUpTo(0) }
            })
        }

        composable(Routes.TEACHER_VERIFICATION_PENDING) {
            TeacherVerificationPendingScreen(onVerified = {
                navController.navigate(Routes.TEACHER_HOME) {
                    popUpTo(Routes.TEACHER_VERIFICATION_PENDING) { inclusive = true }
                }
            })
        }
    }
}
