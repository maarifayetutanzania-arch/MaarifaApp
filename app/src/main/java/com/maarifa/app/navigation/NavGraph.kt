package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maarifa.app.data.model.UserRole
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.AuthViewModelFactory
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.OtpVerificationScreen
import com.maarifa.app.ui.auth.RegisterScreen
import com.maarifa.app.ui.auth.SplashScreen
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

    // Global Listener for Sign Out
    LaunchedEffect(state.isSignedIn) {
        if (!state.isSignedIn && navController.currentDestination?.route != Screen.Splash.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // -------------------- SPLASH --------------------
        composable(Screen.Splash.route) {
            SplashScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        // -------------------- LOGIN --------------------
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // -------------------- REGISTER --------------------
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onRegisterSuccess = { role ->
                    val target = if (role == UserRole.TEACHER) {
                        Screen.TeacherPending.route
                    } else {
                        Screen.StudentHome.route
                    }

                    navController.navigate(target) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToOtp = { phone ->
                    navController.navigate(Screen.OtpVerification.createRoute(phone))
                },
                authViewModel = authViewModel
            )
        }

        // -------------------- OTP --------------------
        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber").orEmpty()

            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                onVerificationSuccess = {
                    val isTeacher = state.profile?.roleEnum == UserRole.TEACHER || state.profile?.role.orEmpty().equals("TEACHER", ignoreCase = true)
                    val target = if (isTeacher) {
                        Screen.TeacherPending.route
                    } else {
                        Screen.StudentHome.route
                    }

                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // -------------------- STUDENT HOME --------------------
        composable(Screen.StudentHome.route) {
            StudentHomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // -------------------- TEACHER HOME --------------------
        composable(Screen.TeacherHome.route) {
            TeacherHomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // -------------------- TEACHER PENDING VERIFICATION --------------------
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
