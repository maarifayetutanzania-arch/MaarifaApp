package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Hakikisha ume-import Screens zako kwa usahihi hapa chini:
// import com.maarifa.app.ui.screens.LoginScreen
// import com.maarifa.app.ui.screens.SignUpScreen
// import com.maarifa.app.ui.screens.HomeScreen

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val HOME = "home"
}

@Composable
Composable NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Login Screen Route
        composable(Routes.LOGIN) {
            // Badala ya kupitisha 'navController' moja kwa moja,
            // tunapitisha Lambda callbacks () -> Unit
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Routes.SIGN_UP)
                }
            )
        }

        // 2. Sign Up Screen Route
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Home Screen Route
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
