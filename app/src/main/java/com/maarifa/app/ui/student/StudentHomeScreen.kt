package com.maarifa.app.ui.student

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private sealed class StudentTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Library : StudentTab("tab_library", "Library", Icons.Default.MenuBook)
    data object Downloads : StudentTab("tab_downloads", "Downloads", Icons.Default.CloudDownload)
    data object Subscription : StudentTab("tab_subscription", "Plan", Icons.Default.Payments)
    data object Profile : StudentTab("tab_profile", "Profile", Icons.Default.AccountCircle)
}

private val studentTabs = listOf(StudentTab.Library, StudentTab.Downloads, StudentTab.Subscription, StudentTab.Profile)

@Composable
fun StudentHomeScreen(onSignedOut: () -> Unit) {
    val innerNav = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by innerNav.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    studentTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                innerNav.navigate(tab.route) {
                                    popUpTo(innerNav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = StudentTab.Library.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(StudentTab.Library.route) {
                LibraryScreen(onOpenMaterial = { id -> innerNav.navigate("student_material/$id") })
            }
            composable(StudentTab.Downloads.route) {
                DownloadsScreen(onOpen = { id -> innerNav.navigate("student_reader/$id") })
            }
            composable(StudentTab.Subscription.route) { SubscriptionScreen() }
            composable(StudentTab.Profile.route) { StudentProfileScreen(onSignedOut = onSignedOut) }

            composable(
                "student_material/{materialId}",
                arguments = listOf(navArgument("materialId") { type = NavType.StringType })
            ) { entry ->
                val materialId = entry.arguments?.getString("materialId").orEmpty()
                MaterialDetailScreen(
                    materialId = materialId,
                    onOpenReader = { innerNav.navigate("student_reader/$materialId") },
                    onNeedsSubscription = { innerNav.navigate(StudentTab.Subscription.route) }
                )
            }
            composable(
                "student_reader/{materialId}",
                arguments = listOf(navArgument("materialId") { type = NavType.StringType })
            ) { entry ->
                val materialId = entry.arguments?.getString("materialId").orEmpty()
                ReaderScreen(materialId = materialId, onBack = { innerNav.popBackStack() })
            }
        }
    }
}
