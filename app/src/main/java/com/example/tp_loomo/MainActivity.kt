package com.example.tp_loomo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tp_loomo.ui.theme.TploomoTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TploomoTheme {
                // Estados: 0 = Splash, 1 = Onboarding, 2 = SignUp, 3 = Login, 4 = MainAppScreen
                var currentScreen by remember { mutableStateOf(1) }
                var currentTab by remember { mutableIntStateOf(0) }

                LaunchedEffect(key1 = true) {
                    delay(3000L)
                    currentScreen = 1
                }

                // Lógica de Navegação
                when (currentScreen) {
                    0 -> SplashScreen()
                    1 -> OnboardingScreen(
                        onSignUp = { currentScreen = 2 },
                        onLogin = { currentScreen = 3 }
                    )
                    2 -> SignUpScreen(
                        onBackClick = { currentScreen = 1 },
                        onLoginClick = { currentScreen = 3 }
                    )
                    3 -> LoginScreen(
                        onBackClick = { currentScreen = 1 },
                        onRegisterClick = { currentScreen = 2 },
                        onLoginClick = { currentScreen = 4 }
                    )
                    4 -> MainAppScreen(
                        currentTab = currentTab,
                        onTabChange = { currentTab = it },
                        onLogout = { currentScreen = 3 },
                        onEditProfile = { currentScreen = 5 },
                        onChangePassword = { currentScreen = 6 }
                    )
                    5 -> EditProfileScreen(
                        onBack = { currentScreen = 4 }
                    )
                    6 -> ChangePasswordScreen(
                        onBack = { currentScreen = 4 }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val LoomoBlue = Color(0xFF1C61A2)

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

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    TploomoTheme {
        SplashScreen()
    }
}