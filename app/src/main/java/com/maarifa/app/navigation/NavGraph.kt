package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.OtpVerificationScreen
import com.maarifa.app.ui.auth.RegisterScreen
import com.maarifa.app.ui.student.StudentHomeScreen
import com.maarifa.app.ui.teacher.TeacherHomeScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object OtpVerification : Screen("otp_verification/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_verification/$phoneNumber"
    }
    data object StudentHome : Screen("student_home")
    data object TeacherHome : Screen("teacher_home")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(
        factory = SimpleViewModelFactory {
            AuthViewModel(maarifaContainer(LocalContext.current.applicationContext).authRepository)
        }
    )
) {
    val state by authViewModel.state.collectAsState()

    val startDestination = when {
        state.isSignedIn && state.profile?.role == "TEACHER" -> Screen.TeacherHome.route
        state.isSignedIn -> Screen.StudentHome.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = {
                    val target = if (state.profile?.role == "TEACHER") Screen.TeacherHome.route else Screen.StudentHome.route
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

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber").orEmpty()
            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                onVerificationSuccess = {
                    val target = if (state.profile?.role == "TEACHER") Screen.TeacherHome.route else Screen.StudentHome.route
                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.StudentHome.route) {
            StudentHomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}
