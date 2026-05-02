package com.example.tp_loomo

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun DashboardManagerScreen() {
    var userName by remember { mutableStateOf("A carregar...") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    val fullName = currentUser.userMetadata?.get("full_name").toString()
                    userName = fullName.replace("\"", "").substringBefore(" ")
                    avatarUrl = currentUser.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
                } else {
                    userName = "Gestor"
                }
            } catch (e: Exception) {
                userName = "Gestor"
            }
        }
    }

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
                if (!avatarUrl.isNullOrEmpty() && avatarUrl != "null") {
                    AsyncImage(
                        model = avatarUrl,
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
                Text(text = "Olá, $userName", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(text = "Bem-vindo de volta!", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cartões de Estatísticas
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

        // Lista de Projetos
        ManagerProjectCard(
            title = "Trabalho Prático Redes",
            pending = 5,
            completed = 3,
            progress = 0.5f,
            bgColors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF))
        )
        Spacer(modifier = Modifier.height(16.dp))
        ManagerProjectCard(
            title = "Projeto IV",
            pending = 2,
            completed = 7,
            progress = 0.75f,
            bgColors = listOf(Color(0xFF434343), Color(0xFF000000))
        )

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