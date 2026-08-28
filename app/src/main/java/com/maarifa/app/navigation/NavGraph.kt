package com.maarifa.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Badilisha au hakikisha imports za screens hapa chini ni sahihi kulingana na mradi wako
import com.maarifa.app.ui.auth.LoginScreen
import com.maarifa.app.ui.auth.SignUpScreen
import com.maarifa.app.ui.auth.SplashScreen

@Composable
fun MaarifaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(Routes.REGISTER) {
            SignUpScreen(navController = navController)
        }
    }
}
