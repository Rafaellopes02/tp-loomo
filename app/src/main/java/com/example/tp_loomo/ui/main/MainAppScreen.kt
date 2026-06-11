package com.example.tp_loomo.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tp_loomo.ui.manager.DashboardManagerScreen
import com.example.tp_loomo.ui.user.DashboardUserScreen
import com.example.tp_loomo.ui.profile.ProfileUserScreen
import com.example.tp_loomo.ui.admin.DashboardAdminScreen
import com.example.tp_loomo.ui.admin.ProjectsAdminScreen
import com.example.tp_loomo.ui.admin.stats.StatsAdminScreen
import com.example.tp_loomo.ui.admin.UsersAdminScreen
import com.example.tp_loomo.ui.components.FloatingBottomNavBar
import android.app.Application
import com.example.tp_loomo.ui.manager.ProjectsManagerScreen
import com.example.tp_loomo.ui.manager.StatsManagerScreen
import com.example.tp_loomo.ui.user.TasksUserScreen
import com.example.tp_loomo.ui.user.HistoryUserScreen
import com.example.tp_loomo.viewmodel.MainViewModel

@Composable
fun MainAppScreen(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    navController: NavController,
    viewModel: MainViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(LocalContext.current.applicationContext as Application)
    )
) {
    val backgroundColor = Color(0xFFFAFAFA)
    val context = LocalContext.current
    val currentRole = viewModel.currentRole

    // Log para diagnosticar o estado do cargo
    android.util.Log.d("NAV_DEBUG", "Role na MainAppScreen: '$currentRole'")

    // 1. Carrega o role no início
    LaunchedEffect(Unit) {
        viewModel.fetchUserRole()
    }

    // 2. Observa o erro de forma explícita
    LaunchedEffect(viewModel.errorMessage) {
        val message = viewModel.errorMessage
        if (!message.isNullOrBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            FloatingBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = onTabChange,
                currentRole = currentRole
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Se ainda está a carregar, mostra apenas o spinner central
            if (currentRole == "loading") {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1C61A2))
                }
            } else {
                // Só processa a navegação quando o cargo já foi definido
                when (currentTab) {
                    0 -> {
                        android.util.Log.d("NAV_DEBUG", "Comparando '$currentRole' com 'project_manager'")
                        when (currentRole) {
                            "admin" -> DashboardAdminScreen(navController = navController)
                            "project_manager" -> DashboardManagerScreen(
                                onProjectClick = { projectId -> navController.navigate("projectDetails/$projectId") }
                            )
                            else -> DashboardUserScreen(
                                onProjectClick = { projectId -> navController.navigate("projectDetailsUser/$projectId") },
                                onTaskClick = { taskId -> navController.navigate("taskDetails/$taskId") },
                                onViewAllTasksClick = { onTabChange(1) }
                            )
                        }
                    }
                    1 -> {
                        when (currentRole) {
                            "admin" -> ProjectsAdminScreen(navController = navController)
                            "project_manager" -> ProjectsManagerScreen(
                                onProjectClick = { projectId -> navController.navigate("projectDetails/$projectId") }
                            )
                            else -> TasksUserScreen(
                                onTaskClick = { taskId -> navController.navigate("taskDetails/$taskId") }
                            )
                        }
                    }
                    2 -> {
                        when (currentRole) {
                            "admin" -> StatsAdminScreen()
                            "project_manager" -> StatsManagerScreen()
                            else -> HistoryUserScreen()
                        }
                    }
                    3 -> {
                        if (currentRole == "admin") {
                            UsersAdminScreen()
                        } else {
                            key(currentTab) {
                                ProfileUserScreen(onLogout, onEditProfile, onChangePassword)
                            }
                        }
                    }
                    4 -> {
                        if (currentRole == "admin") {
                            ProfileUserScreen(onLogout, onEditProfile, onChangePassword)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = Color.Gray)
    }
}