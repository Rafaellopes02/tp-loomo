package com.example.tp_loomo.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.viewmodel.ProjectDetailsViewModel

@Composable
fun ProjectDetailsScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    viewModel: ProjectDetailsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("All") }

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
    }

    val project = viewModel.project
    val teamMembers = viewModel.teamMembers
    val isLoading = viewModel.isLoading

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            Text("Projeto não encontrado", color = Color.Gray)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- CABEÇALHO (HEADER) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0xFFB5B5B5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(60.dp)) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(50.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Projeto", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Detalhes do projeto", color = Color.White, fontSize = 16.sp)
                }

                IconButton(onClick = { /* Opções futuras */ }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Mais", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }

            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp)
            ) {
                OverlappingAvatars(avatarUrls = teamMembers.map { it.avatar_url })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CONTEÚDO DO PROJETO ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(text = project.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = project.description ?: "Sem descrição.", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECÇÃO DO PRAZO E PROGRESSO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEAEA)).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Prazo-Final: ${project.end_date ?: "Sem prazo"}",
                        color = Color(0xFFD32F2F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(text = "0%", color = Color(0xFF1C61A2), fontSize = 14.sp, fontWeight = FontWeight.Bold) // A ligar às tarefas no futuro
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Progresso Customizada
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFEEEEEE))) {
                Box(modifier = Modifier.fillMaxWidth(0.0f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFF1C61A2)))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- FILTROS (TABS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                CustomFilterChip("All", selectedTab == "All") { selectedTab = "All" }
                CustomFilterChip("OnGoing", selectedTab == "OnGoing") { selectedTab = "OnGoing" }
                CustomFilterChip("Concluded", selectedTab == "Concluded") { selectedTab = "Concluded" }
            }
        }
    }
}

// COMPONENTES AUXILIARES INALTERADOS
@Composable
fun OverlappingAvatars(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        visibleAvatars.forEach { url ->
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (!url.isNullOrEmpty() && url != "null") {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                }
            }
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("+$remaining", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CustomFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(if (isSelected) Color(0xFF1C61A2) else Color(0xFFF3F3F3))
            .clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}