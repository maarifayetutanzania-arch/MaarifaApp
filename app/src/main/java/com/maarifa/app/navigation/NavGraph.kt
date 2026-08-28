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
import com.maarifa.app.ui.auth.RegisterScreen
import com.maarifa.app.ui.auth.SplashScreen
// Ongeza imports za screens zingine unazozitumia (OtpScreen, StudentHomeScreen, TeacherHomeScreen n.k.)

@Composable
fun MaarifaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    val container = maarifaContainer()
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = container.authRepository,
            authService = container.authService
        )
    )

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

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(Routes.REGISTER) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            RegisterScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = {
                    // Baada ya kusajili, nenda Student Home (au Teacher kulingana na role)
                    navController.navigate(Routes.STUDENT_HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                passedUid = currentUid          // ← Hii ndiyo inatatua "User ID not found"
            )
        }

        // OTP Screen
        composable(
            route = Routes.OTP,
            arguments = listOf(
                navArgument("verificationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            // TODO: Weka OtpScreen yako hapa
            // OtpScreen(
            //     verificationId = verificationId,
            //     authViewModel = authViewModel,
            //     navController = navController
            // )
        }

        // ==================== STUDENT ====================
        composable(Routes.STUDENT_HOME) {
            // StudentHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        // ==================== TEACHER ====================
        composable(Routes.TEACHER_HOME) {
            // TeacherHomeScreen(navController = navController, authViewModel = authViewModel)
        }

        // Ongeza routes zingine unazohitaji baadaye...
    }
}
