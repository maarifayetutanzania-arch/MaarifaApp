package com.maarifa.app.ui.auth

// ... imports zote za Android/Compose ...
import androidx.navigation.NavController
import com.maarifa.app.navigation.Routes

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {
    // ... logic nzima ya UI ...

    // Kwenye kitufe cha "Tengeneza Akaunti >":
    Text(
        text = "Tengeneza Akaunti >",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E7F55),
        modifier = Modifier.clickable {
            navController.navigate(Routes.REGISTER)
        }
    )
}
