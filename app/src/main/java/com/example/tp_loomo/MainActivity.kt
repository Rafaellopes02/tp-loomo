package com.example.tp_loomo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tp_loomo.ui.auth.LoginScreen
import com.example.tp_loomo.ui.auth.OnboardingScreen
import com.example.tp_loomo.ui.auth.SignUpScreen
import com.example.tp_loomo.ui.main.MainAppScreen
import com.example.tp_loomo.ui.profile.ChangePasswordScreen
import com.example.tp_loomo.ui.profile.EditProfileScreen
import com.example.tp_loomo.ui.project.ProjectDetailsScreen
import com.example.tp_loomo.ui.theme.TploomoTheme
import com.example.tp_loomo.data.remote.api.supabase
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TploomoTheme {
                val navController = rememberNavController()
                var currentTab by remember { mutableIntStateOf(0) }

                NavHost(navController = navController, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen {
                            val user = supabase.auth.currentUserOrNull()
                            if (user != null) {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            } else {
                                navController.navigate("onboarding") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }
                    }

                    composable("onboarding") {
                        OnboardingScreen(
                            onSignUp = { navController.navigate("signup") },
                            onLogin = { navController.navigate("login") }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onBackClick = { navController.popBackStack() },
                            onLoginClick = { navController.navigate("login") }
                        )
                    }

                    composable("login") {
                        LoginScreen(
                            onBackClick = { navController.popBackStack() },
                            onRegisterClick = { navController.navigate("signup") },
                            onLoginClick = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainAppScreen(
                            currentTab = currentTab,
                            onTabChange = { currentTab = it },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            },
                            onEditProfile = { navController.navigate("editProfile") },
                            onChangePassword = { navController.navigate("changePassword") },
                            navController = navController
                        )
                    }

                    composable("editProfile") {
                        EditProfileScreen(onBack = { navController.popBackStack() })
                    }

                    composable("changePassword") {
                        ChangePasswordScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = "projectDetails/{projectId}",
                        arguments = listOf(navArgument("projectId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val projectId = backStackEntry.arguments?.getInt("projectId") ?: return@composable

                        ProjectDetailsScreen(
                            projectId = projectId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val loomoBlue = Color(0xFF1C61A2)

    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(loomoBlue),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_loomo_logo),
            contentDescription = "Símbolo Loomo",
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_loomo_logo_text),
            contentDescription = "Texto Loomo",
            modifier = Modifier.width(200.dp)
        )
    }
}