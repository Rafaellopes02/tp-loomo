package com.example.tp_loomo.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.viewmodel.AdminViewModel

@Composable
fun ProjectsAdminScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showAddProjectModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        adminViewModel.loadAllProjects()
    }

    val projects = adminViewModel.allProjectsList
    val isLoading = adminViewModel.isLoading

    val filteredProjects = projects.filter { project ->
        when (selectedFilter) {
            "Andamento" -> project.status == "active"
            "Concluidos" -> project.status == "concluded"
            else -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(text = "Todos Os Projetos", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Veja todos os projetos da app", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                FigmaFilterChip("Todas", selectedFilter == "Todas") { selectedFilter = "Todas" }
                FigmaFilterChip("Andamento", selectedFilter == "Andamento") { selectedFilter = "Andamento" }
                FigmaFilterChip("Concluidos", selectedFilter == "Concluidos") { selectedFilter = "Concluidos" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1C61A2))
                }
            } else if (filteredProjects.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "Nenhum projeto encontrado.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp)
                ) {
                    items(filteredProjects) { project ->
                        AdminProjectListCard(
                            project = project,
                            onClick = {
                                navController.navigate("projectDetails/${project.id}")
                            }
                        )
                    }
                }
            }
        }


    }
}

@Composable
fun FigmaFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF1C61A2) else Color(0xFFF0F0F0))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdminProjectListCard(project: Project, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC)) // Gradiente subtil abstrato (substitui a imagem 3D)
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 16.dp)
                ) {
                    val mockAvatars = listOf("url1", "url2", "url3", "url4")
                    ProjectOverlappingAvatars(avatarUrls = mockAvatars)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Opções", tint = Color(0xFF1C61A2))
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Gestor: Tiago Melo",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(text = "50%", color = Color(0xFF1C61A2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFEEEEEE))) {
                    Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(Color(0xFF1C61A2)))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "5 tarefas pendentes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2))
                    Text(text = "3 concluídas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) // Verde
                }
            }
        }
    }
}

@Composable
fun ProjectOverlappingAvatars(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
        visibleAvatars.forEach { url ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color(0xFFFFB74D)), // Cor de fundo temporária
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("+$remaining", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}