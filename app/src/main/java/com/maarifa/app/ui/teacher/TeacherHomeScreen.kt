package com.maarifa.app.ui.teacher

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maarifa.app.data.model.TeacherVerificationStatus
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer

private sealed class TeacherTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Dashboard : TeacherTab("teacher_tab_dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Upload : TeacherTab("teacher_tab_upload", "Upload", Icons.Default.PostAdd)
    data object Materials : TeacherTab("teacher_tab_materials", "Materials", Icons.Default.Folder)
    data object Earnings : TeacherTab("teacher_tab_earnings", "Earnings", Icons.Default.Payments)
}

private val teacherTabs = listOf(TeacherTab.Dashboard, TeacherTab.Upload, TeacherTab.Materials, TeacherTab.Earnings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(onSignedOut: () -> Unit) {
    val container = maarifaContainer()
    val dashboardVm: TeacherDashboardViewModel = viewModel(
        factory = SimpleViewModelFactory { TeacherDashboardViewModel(container.teacherRepository, container.authRepository) }
    )
    val dashboardState by dashboardVm.state.collectAsState()
    val innerNav = rememberNavController()

    // Non-verified teachers should not reach the upload/earnings tools even if they
    // navigate here directly — the pending screen owns routing until VERIFIED.
    if (dashboardState.teacher?.verificationStatus != TeacherVerificationStatus.VERIFIED.name && !dashboardState.isLoading) {
        TeacherVerificationPendingScreen(onVerified = {})
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maarifa Teacher", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { dashboardVm.signOut(); onSignedOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Sign out", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            val backStackEntry by innerNav.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    teacherTabs.forEach { tab ->
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
        NavHost(navController = innerNav, startDestination = TeacherTab.Dashboard.route, modifier = Modifier.padding(padding)) {
            composable(TeacherTab.Dashboard.route) { TeacherDashboardScreen() }
            composable(TeacherTab.Upload.route) { UploadMaterialScreen(onUploaded = { innerNav.navigate(TeacherTab.Materials.route) }) }
            composable(TeacherTab.Materials.route) { TeacherMaterialsScreen() }
            composable(TeacherTab.Earnings.route) { TeacherEarningsScreen() }
        }
    }
}
