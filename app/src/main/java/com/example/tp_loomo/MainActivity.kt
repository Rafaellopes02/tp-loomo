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
import com.example.tp_loomo.ui.theme.TploomoTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TploomoTheme {
                val navController = rememberNavController()

                // Variável para controlar a Tab ativa no MainAppScreen
                var currentTab by remember { mutableIntStateOf(0) }

                NavHost(navController = navController, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen {
                            navController.navigate("onboarding") {
                                popUpTo("splash") { inclusive = true }
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
                                    popUpTo(0) // Limpa toda a stack ao fazer logout
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
                        route = "projectDetails/{title}/{desc}/{deadline}",
                        arguments = listOf(
                            navArgument("title") { type = NavType.StringType },
                            navArgument("desc") { type = NavType.StringType },
                            navArgument("deadline") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title")?.replace("_", " ") ?: ""
                        val desc = backStackEntry.arguments?.getString("desc")?.replace("_", " ") ?: ""
                        val deadline = backStackEntry.arguments?.getString("deadline")?.replace("_", " ") ?: ""

                        ProjectDetailsScreen(
                            onBackClick = { navController.popBackStack() },
                            projectTitle = title,
                            projectDesc = desc,
                            deadline = deadline,
                            avatarUrls = listOf(null, null, null) // Simulamos os avatars iniciais
                        )
                    }
                }
            }
        }
    }
}

// Repara que a SplashScreen está CÁ FORA, no nível principal do ficheiro!
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val LoomoBlue = Color(0xFF1C61A2)

    LaunchedEffect(key1 = true) {
        delay(3000)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoomoBlue),
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