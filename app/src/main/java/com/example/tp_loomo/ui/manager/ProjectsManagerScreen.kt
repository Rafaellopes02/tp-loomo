package com.example.tp_loomo.ui.manager

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.viewmodel.ProjectUiModel
import com.example.tp_loomo.viewmodel.ProjectsManagerViewModel

@Composable
fun ProjectsManagerScreen(
    onProjectClick: (Int) -> Unit = {},
    viewModel: ProjectsManagerViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("Todas") }

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    val projectsList = viewModel.projectsList
    val isLoading = viewModel.isLoading

    // --- LÓGICA DE FILTRAGEM (PROGRESSO + STATUS DA BASE DE DADOS) ---
    val filteredProjects = projectsList.filter {
        val status = it.project.status?.lowercase()
        when (selectedFilter) {
            "Andamento" -> it.progress < 100 && status != "completed" && status != "concluded"
            "Concluidos" -> it.progress == 100 || status == "completed" || status == "concluded"
            else -> true
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp)
    ) {
        // --- CABEÇALHO ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.your_projects_title), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.view_all_projects_subtitle), fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- FILTROS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChipProject(stringResource(id = R.string.all), selectedFilter == "Todas") { selectedFilter = "Todas" }
                Spacer(modifier = Modifier.width(12.dp))
                FilterChipProject(stringResource(id = R.string.filter_in_progress), selectedFilter == "Andamento") { selectedFilter = "Andamento" }
                Spacer(modifier = Modifier.width(12.dp))
                FilterChipProject(stringResource(id = R.string.filter_completed), selectedFilter == "Concluidos") { selectedFilter = "Concluidos" }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- CARTÕES DOS PROJETOS ---
        if (filteredProjects.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.no_projects_found),
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            items(filteredProjects) { item ->
                ProjectLargeCard(
                    uiModel = item,
                    onClick = { item.project.id?.let { onProjectClick(it) } }
                )
            }
        }
    }
}

@Composable
fun ProjectLargeCard(uiModel: ProjectUiModel, onClick: () -> Unit) {
    val project = uiModel.project
    val avatars = uiModel.avatars

    var currentCover by remember(project.cover_url) {
        mutableStateOf<Any?>(
            when (project.cover_url) {
                "fundo_preto" -> R.drawable.fundo_preto
                "fundo_rosa" -> R.drawable.fundo_rosa
                "fundo_azul" -> R.drawable.fundo_azul
                "fundo_branco" -> R.drawable.fundo_branco
                else -> project.cover_url
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // CAPA E AVATARES
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8))))
            ) {
                if (currentCover != null) {
                    AsyncImage(model = currentCover, contentDescription = stringResource(id = R.string.cover_content_desc), modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))

                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp)) {
                    OverlappingAvatarsCard(avatarUrls = avatars)
                }
            }

            // DADOS DO PROJETO
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = project.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Icon(Icons.Default.MoreHoriz, contentDescription = stringResource(id = R.string.more_options_content_desc), tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Percentagem e Barra
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(text = "${uiModel.progress}%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C61A2))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE0E0E0))) {
                    Box(modifier = Modifier.fillMaxWidth(uiModel.progress / 100f).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Color(0xFF1C61A2)))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rodapé com Tarefas Pendentes e Concluídas
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(id = R.string.pending_tasks_count, uiModel.pendingTasks),
                        color = Color(0xFF1C61A2),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.completed_tasks_count, uiModel.completedTasks),
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipProject(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(if (isSelected) Color(0xFF1C61A2) else Color.White).clickable { onClick() }.padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OverlappingAvatarsCard(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        visibleAvatars.forEach { url ->
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFB74D)), contentAlignment = Alignment.Center) {
                if (!url.isNullOrEmpty() && url != "null") {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        if (remaining > 0) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                Text("+$remaining", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}