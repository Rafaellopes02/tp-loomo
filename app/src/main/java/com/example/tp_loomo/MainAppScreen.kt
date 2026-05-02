package com.example.tp_loomo

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.serialization.Serializable
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

@Serializable
data class UserRole(val role: String)

@Composable
fun MainAppScreen(
    currentTab: Int,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit
) {

    val backgroundColor = Color(0xFFFAFAFA)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentRole by remember { mutableStateOf("loading") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val profile = supabase.postgrest["profiles"]
                        .select(columns = Columns.list("role")) { filter { eq("id", userId) } }
                        .decodeSingle<UserRole>()

                    currentRole = profile.role.trim().lowercase()

                } else {
                    currentRole = "user"
                }
            } catch (e: Exception) {
                currentRole = "user"
                Toast.makeText(context, "Erro BD: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
                    // SEPARADOR 0: HOME
                    when (currentRole) {
                        "admin" -> DashboardAdminScreen()
                        "project_manager" -> DashboardManagerScreen()
                        "loading" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF1C61A2))
                        }
                        else -> DashboardUserScreen()
                    }
                }
                1 -> {
                    // SEPARADOR 1: PROJETOS
                    PlaceholderScreen("Ecrã de Projetos em Construção")
                }
                2 -> {
                    // SEPARADOR 2: ESTATÍSTICAS (Admin/Gestor) ou HISTÓRICO (User)
                    when (currentRole) {
                        "admin", "project_manager" -> PlaceholderScreen("Ecrã de Estatísticas em Construção")
                        else -> PlaceholderScreen("Ecrã de Histórico em Construção")
                    }
                }
                3 -> {
                    // SEPARADOR 3: UTILIZADORES (Admin) ou PERFIL (Gestor/User)
                    when (currentRole) {
                        "admin" -> UsersAdminScreen()
                        else -> ProfileUserScreen(onLogout = onLogout, onEditProfile = onEditProfile, onChangePassword = onChangePassword)
                    }
                }
                4 -> {
                    // SEPARADOR 4: PERFIL (Apenas para o Admin, que é o único com 5 botões)
                    if (currentRole == "admin") {
                        ProfileUserScreen(onLogout = onLogout, onEditProfile = onEditProfile, onChangePassword = onChangePassword)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Color.Gray)
    }
}