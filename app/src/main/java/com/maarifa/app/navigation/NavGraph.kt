package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Imports za UI Screens na AuthViewModel
import com.maarifa.app.ui.auth.AuthViewModel
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.RegisterScreen // au SignUpScreen kulingana na jina la file lako
import com.maarifa.app.ui.auth.SplashScreen

@Composable
fun MaarifaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
    authViewModel: AuthViewModel = viewModel() // Inatoa default ViewModel instance
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.REGISTER) {
            // Kama Screen yako inaitwa RegisterScreen:
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}
