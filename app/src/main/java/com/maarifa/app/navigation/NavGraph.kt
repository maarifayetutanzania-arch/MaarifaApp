package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.AuthViewModelFactory
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.OtpVerificationScreen
import com.maarifa.app.ui.auth.RegisterScreen
import com.maarifa.app.ui.auth.SplashScreen
import com.maarifa.app.ui.auth.WelcomeScreen
import com.maarifa.app.ui.home.StudentHomeScreen
import com.maarifa.app.ui.home.TeacherHomeScreen

@Composable
fun MaarifaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
    authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = maarifaContainer().authRepository,
            authService = maarifaContainer().authService
        )
    )
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== AUTH ====================
        composable(Routes.SPLASH) {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(navController = navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(Routes.REGISTER) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            RegisterScreen(
                authViewModel = authViewModel,
                navController = navController,
                passedUid = currentUid
            )
        }

        // OTP Screen
        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("verificationId") { type = NavType.StringType },
                navArgument("phone") { 
                    type = NavType.StringType 
                    defaultValue = "" 
                }
            )
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            
            OtpVerificationScreen(
                verificationId = verificationId,
                phoneNumber = phone,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        // ==================== STUDENT ====================
        composable(Routes.STUDENT_HOME) {
            StudentHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Routes.SEARCH) { /* SearchScreen */ }
        composable(Routes.SUBSCRIPTION) { /* SubscriptionScreen */ }
        composable(Routes.DOWNLOADS) { /* DownloadsScreen */ }
        composable(Routes.STUDENT_PROFILE) { /* StudentProfileScreen */ }

        // ==================== TEACHER ====================
        composable(Routes.TEACHER_HOME) {
            TeacherHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Routes.TEACHER_VERIFICATION_PENDING) { /* TeacherPendingScreen */ }
        composable(Routes.UPLOAD_MATERIAL) { /* UploadMaterialScreen */ }
        composable(Routes.TEACHER_MATERIALS) { /* TeacherMaterialsScreen */ }
        composable(Routes.TEACHER_EARNINGS) { /* TeacherEarningsScreen */ }
        composable(Routes.TEACHER_PROFILE) { /* TeacherProfileScreen */ }
    }
}
