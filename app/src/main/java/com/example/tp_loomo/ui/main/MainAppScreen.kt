package com.example.tp_loomo.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.tp_loomo.ui.admin.UsersAdminScreen
import com.example.tp_loomo.ui.components.FloatingBottomNavBar
import com.example.tp_loomo.ui.manager.ProjectsManagerScreen
import com.example.tp_loomo.ui.user.TasksUserScreen
import com.example.tp_loomo.viewmodel.MainViewModel

@Composable
fun MainAppScreen(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val backgroundColor = Color(0xFFFAFAFA)
    val context = LocalContext.current
    val currentRole = viewModel.currentRole

    LaunchedEffect(Unit) {
        viewModel.fetchUserRole()
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            Toast.makeText(context, "Erro BD: $error", Toast.LENGTH_LONG).show()
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
            when (currentTab) {
                0 -> {
                    when (currentRole) {
                        "admin" -> DashboardAdminScreen(navController = navController)
                        "project_manager" -> DashboardManagerScreen()
                        "loading" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF1C61A2))
                        }
                        else -> DashboardUserScreen(
                            onProjectClick = { projectId ->
                                navController.navigate("projectDetailsUser/$projectId")
                            },
                            // 1. Ao clicar na tarefa, abre os detalhes da mesma
                            onTaskClick = { taskId ->
                                navController.navigate("taskDetails/$taskId")
                            },
                            // 2. Ao clicar em "Ver todas", muda a barra em baixo para o separador 1 (Tarefas)
                            onViewAllTasksClick = {
                                onTabChange(1)
                            }
                        )
                    }
                }
                1 -> {
                    when (currentRole) {
                        "admin" -> ProjectsAdminScreen(navController = navController)
                        "project_manager" -> ProjectsManagerScreen(
                            onProjectClick = { projectId ->
                                navController.navigate("projectDetails/$projectId")
                            }
                        )
                        else -> TasksUserScreen(
                            onTaskClick = { taskId ->
                                navController.navigate("taskDetails/$taskId")
                            }
                        )
                    }
                }
                2 -> {
                    if (currentRole == "admin" || currentRole == "project_manager") {
                        PlaceholderScreen("Ecrã de Estatísticas em Construção")
                    } else {
                        PlaceholderScreen("Ecrã de Histórico em Construção")
                    }
                }
                3 -> {
                    if (currentRole == "admin") {
                        UsersAdminScreen()
                    } else {
                        ProfileUserScreen(onLogout, onEditProfile, onChangePassword)
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

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = Color.Gray)
    }
}