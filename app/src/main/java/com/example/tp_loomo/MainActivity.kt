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

                    // SUBSTITUI A TUA ROTA DOS DETALHES POR ESTA:
                    composable(
                        route = "projectDetails/{id}/{title}/{desc}/{deadline}?fotos_url={fotos_url}&avatars={avatars}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.IntType },
                            navArgument("title") { type = NavType.StringType },
                            navArgument("desc") { type = NavType.StringType },
                            navArgument("deadline") { type = NavType.StringType },
                            navArgument("fotos_url") { type = NavType.StringType; nullable = true },
                            navArgument("avatars") { type = NavType.StringType; nullable = true }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: 0
                        val title = backStackEntry.arguments?.getString("title")?.replace("_", " ") ?: ""
                        val desc = backStackEntry.arguments?.getString("desc")?.replace("_", " ") ?: ""
                        val deadline = backStackEntry.arguments?.getString("deadline")?.replace("_", " ") ?: ""

                        val fotosUrlRaw = backStackEntry.arguments?.getString("fotos_url")
                        val fotosUrl = if (fotosUrlRaw == "null") null else fotosUrlRaw

                        // DESCODIFICA OS AVATARES (Desfaz o Base64 à prova de bala)
                        val avatarsRaw = backStackEntry.arguments?.getString("avatars")
                        val avatarUrlsList = if (avatarsRaw != null && avatarsRaw != "null" && avatarsRaw.isNotBlank()) {
                            try {
                                val decodedBytes = android.util.Base64.decode(avatarsRaw, android.util.Base64.URL_SAFE)
                                val decodedString = String(decodedBytes, Charsets.UTF_8)
                                decodedString.split("|") // Separa as fotos todas de novo
                            } catch (e: Exception) {
                                listOf(null) // Proteção para nunca crashar
                            }
                        } else {
                            listOf(null) // Se a app não receber avatares, desenha 1 boneco cinzento
                        }

                        ProjectDetailsScreen(
                            projectId = id,
                            onBackClick = { navController.popBackStack() },
                            projectTitle = title,
                            projectDesc = desc,
                            deadline = deadline,
                            avatarUrls = avatarUrlsList,
                            fotosUrlDaBd = fotosUrl
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