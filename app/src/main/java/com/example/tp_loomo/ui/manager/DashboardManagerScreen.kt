package com.example.tp_loomo.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.viewmodel.ManagerViewModel
import com.example.tp_loomo.viewmodel.ProfileViewModel

@Composable
fun DashboardManagerScreen(
    managerViewModel: ManagerViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        managerViewModel.loadDashboardData()
        profileViewModel.loadProfile()
    }

// Recarrega o perfil quando o SyncWorker terminar (observa o WorkManager)
    LaunchedEffect(Unit) {
        androidx.work.WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("profile_sync_unique")
            .collect { infoList ->
                val finished = infoList.any { it.state == androidx.work.WorkInfo.State.SUCCEEDED }
                if (finished) {
                    profileViewModel.loadProfile()
                }
            }
    }

    val userData = profileViewModel.userData
    val projects = managerViewModel.managedProjects
    val isLoading = managerViewModel.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Cabeçalho
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (!userData?.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = userData?.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val primeiroNome = userData?.nomeCompleto?.split(" ")?.first() ?: "Gestor"
                Text(text = "Olá, $primeiroNome", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(text = "Bem-vindo de volta!", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cartões de Estatísticas (Placeholder)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(title = "Tarefas Pendentes", value = "8", bgColor = Color(0xFF9EBAE1), modifier = Modifier.weight(1f))
            StatCard(title = "Tarefas Concluídas", value = "10", bgColor = Color(0xFF90D992), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Secção de Projetos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Meus Projetos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "Ver todas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Estado de Carregamento ou Lista
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (projects.isEmpty()) {
            Text(text = "Ainda não tens projetos a teu cargo.", color = Color.Gray)
        } else {
            // Lista Dinâmica de Projetos
            val palette = listOf(
                listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)),
                listOf(Color(0xFFa18cd1), Color(0xFFfbc2eb)),
                listOf(Color(0xFF84fab0), Color(0xFF8fd3f4)),
                listOf(Color(0xFF434343), Color(0xFF000000))
            )

            projects.forEachIndexed { index, project ->
                ManagerProjectCard(
                    title = project.name,
                    pending = 0,
                    completed = 0,
                    progress = 0.0f,
                    bgColors = palette[index % palette.size]
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatCard(title: String, value: String, bgColor: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2))
            Text(text = value, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C61A2))
        }
    }
}

@Composable
fun ManagerProjectCard(title: String, pending: Int, completed: Int, progress: Float, bgColors: List<Color>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(brush = Brush.horizontalGradient(colors = bgColors))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Opções", tint = Color(0xFF1C61A2))
                }
                Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2), modifier = Modifier.align(Alignment.End))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF1C61A2),
                    trackColor = Color(0xFFE0E0E0),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "$pending tarefas pendentes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2))
                    Text(text = "$completed concluídas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
        }
    }
}