package com.example.tp_loomo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // IMPORTANTE: Import do Coil para a foto
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun DashboardUserScreen() {
    var userName by remember { mutableStateOf("A carregar...") }
    var avatarUrl by remember { mutableStateOf<String?>(null) } // Nova variável para guardar o URL da foto
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    val fullName = currentUser.userMetadata?.get("full_name").toString()
                    userName = fullName.replace("\"", "")

                    // Vai buscar a foto, exatamente como fizemos no Perfil
                    avatarUrl = currentUser.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
                } else {
                    userName = "Visitante"
                }
            } catch (e: Exception) {
                userName = "Visitante"
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

        // Passamos também o URL da foto para o Cabeçalho
        DashboardHeader(nome = userName, avatarUrl = avatarUrl)

        Spacer(modifier = Modifier.height(32.dp))

        ProjectHighlightCard()

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tarefas de Hoje", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = "Ver todas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TaskCard(title = "Desenvolver Protótipo Figma", project = "Trabalho Prático Redes")
        Spacer(modifier = Modifier.height(12.dp))
        TaskCard(title = "Criar plano no Trello", project = "Trabalho Prático Redes")

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ATUALIZADO: Agora recebe o avatarUrl também
@Composable
fun DashboardHeader(nome: String, avatarUrl: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            // Lógica para mostrar a foto ou o bonequinho cinzento
            if (!avatarUrl.isNullOrEmpty() && avatarUrl != "null") {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                // ATUALIZADO: Junta a string do "Hello" com a variável "$nome"
                text = "${stringResource(id = R.string.hello)}$nome",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = stringResource(id = R.string.login_subtitle),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProjectHighlightCard() {
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
                    .height(120.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF), Color(0xFF1C61A2))
                        )
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trabalho Prático Redes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Mais opções", tint = Color(0xFF1C61A2))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Desenvolver Protótipo Figma - ",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Até 16 Mai 2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(title: String, project: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatListBulleted,
                contentDescription = "Tarefa",
                tint = Color(0xFF1C61A2),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = project,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardUserScreenPreview() {
    DashboardUserScreen()
}