package com.example.tp_loomo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MainAppScreen(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit
) {

    val backgroundColor = Color(0xFFFAFAFA)
    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            FloatingBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = onTabChange
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (currentTab) {
                0 -> DashboardUserScreen()
                1 -> PlaceholderScreen("Ecrã de Projetos em Construção")
                2 -> PlaceholderScreen("Ecrã de Calendário em Construção")
                3 -> ProfileUserScreen(onLogout = onLogout, onEditProfile = onEditProfile, onChangePassword = onChangePassword)
            }
        }
    }
}

// Um ecrã temporário só para ver as abas a funcionar
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Color.Gray)
    }
}