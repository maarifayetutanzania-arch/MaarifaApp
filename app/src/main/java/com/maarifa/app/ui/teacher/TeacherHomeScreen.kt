package com.maarifa.app.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maarifa.app.ui.auth.AuthViewModel

private sealed class TeacherTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Dashboard : TeacherTab("teacher_tab_dashboard", "Dashboard", Icons.Default.GridView)
    data object Upload : TeacherTab("teacher_tab_upload", "Upload", Icons.Default.PostAdd)
    data object Materials : TeacherTab("teacher_tab_materials", "Materials", Icons.Default.Folder)
    data object Earnings : TeacherTab("teacher_tab_earnings", "Earnings", Icons.Default.AccountBalanceWallet)
}

private val teacherTabs = listOf(
    TeacherTab.Dashboard,
    TeacherTab.Upload,
    TeacherTab.Materials,
    TeacherTab.Earnings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val innerNav = rememberNavController()

    val primaryGreen = Color(0xFF1E7F55)
    val darkGreen = Color(0xFF1B5E20)
    val lightGreenIndicator = Color(0xFFE8F5E9)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(darkGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Maarifa ",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = darkGreen
                                )
                                Text(
                                    text = "Teacher",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryGreen
                                )
                            }
                            Text(
                                text = "Learn • Teach • Grow",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        IconButton(onClick = { /* Open Notifications */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF374151)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(x = (-10).dp, y = 10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }

                    IconButton(
                        onClick = { authViewModel.signOut() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile & Sign Out",
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            val backStackEntry by innerNav.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            Column {
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    teacherTabs.forEach { tab ->
                        val isSelected = currentRoute?.hierarchy?.any { it.route == tab.route } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                innerNav.navigate(tab.route) {
                                    popUpTo(innerNav.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = primaryGreen,
                                selectedTextColor = primaryGreen,
                                indicatorColor = lightGreenIndicator,
                                unselectedIconColor = Color(0xFF9CA3AF),
                                unselectedTextColor = Color(0xFF9CA3AF)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNav,
            startDestination = TeacherTab.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(TeacherTab.Dashboard.route) { TeacherDashboardScreen() }
            composable(TeacherTab.Upload.route) {
                UploadMaterialScreen(onUploaded = {
                    innerNav.navigate(TeacherTab.Materials.route)
                })
            }
            composable(TeacherTab.Materials.route) { TeacherMaterialsScreen() }
            composable(TeacherTab.Earnings.route) { TeacherEarningsScreen() }
        }
    }
}
