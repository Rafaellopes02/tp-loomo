package com.example.tp_loomo.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.FormatListBulleted
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.ui.admin.CardProfileRow
import com.example.tp_loomo.viewmodel.ProjectDetailsViewModel
import com.example.tp_loomo.data.remote.api.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

@Composable
fun ProjectDetailsUserScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    onTaskClick: (taskId: Int) -> Unit = {},
    viewModel: ProjectDetailsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Todas") }
    var realManagerName by remember { mutableStateOf("A carregar...") }
    var realManagerAvatar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
    }

    val project = viewModel.project
    val teamMembers = viewModel.teamMembers
    val projectTasks = viewModel.projectTasks
    val isLoading = viewModel.isLoading

    LaunchedEffect(project?.project_manager_id) {
        if (project?.project_manager_id != null) {
            try {
                val managerProfile = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("full_name", "avatar_url")) {
                        filter { eq("id", project.project_manager_id) }
                    }.decodeSingleOrNull<CardProfileRow>()

                if (managerProfile != null) {
                    realManagerName = managerProfile.full_name ?: "Sem Nome"
                    realManagerAvatar = managerProfile.avatar_url
                } else {
                    realManagerName = "Desconhecido"
                }
            } catch (e: Exception) {
                realManagerName = "Erro"
            }
        } else {
            realManagerName = "Sem Gestor"
        }
    }

    val projectAvatars = remember(realManagerAvatar, teamMembers) {
        val combinedList = mutableListOf<String?>()
        combinedList.add(realManagerAvatar)
        combinedList.addAll(teamMembers.map { it.avatar_url })
        combinedList.distinct()
    }

    var currentCover by remember(project?.cover_url) {
        mutableStateOf<Any?>(
            when (project?.cover_url) {
                "fundo_preto" -> R.drawable.fundo_preto
                "fundo_rosa" -> R.drawable.fundo_rosa
                "fundo_azul" -> R.drawable.fundo_azul
                "fundo_branco" -> R.drawable.fundo_branco
                else -> project?.cover_url
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            Text("Projeto não encontrado", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(brush = Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8))))
            ) {
                if (currentCover != null) {
                    AsyncImage(
                        model = currentCover, contentDescription = "Capa",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.29f)))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Projeto", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Veja detalhes do projeto", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.size(48.dp)) // Mantém o título centrado
                }

                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp)) {
                    OverlappingAvatarsUser(avatarUrls = projectAvatars)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(text = project.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = project.description ?: "Sem descrição.", fontSize = 15.sp, color = Color.Gray, lineHeight = 22.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Gestor: $realManagerName", fontSize = 15.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEBEE)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(text = "Prazo-Final: ${project.end_date ?: "Sem prazo"}", color = Color(0xFFD32F2F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "50%", color = Color(0xFF1C61A2), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE0E0E0))) {
                    Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Color(0xFF1C61A2)))
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                CustomFilterChipUser("Todas", selectedTab == "Todas") { selectedTab = "Todas" }
                CustomFilterChipUser("Andamento", selectedTab == "Andamento") { selectedTab = "Andamento" }
                CustomFilterChipUser("Concluído", selectedTab == "Concluído") { selectedTab = "Concluído" }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (projectTasks.isEmpty()) {
            item {
                Text("Sem tarefas para apresentar.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            items(projectTasks) { task ->
                TaskItemCardUser(
                    title = task.title,
                    time = task.due_date ?: "Sem data",
                    onClick = { task.id?.let { onTaskClick(it) } }
                )
            }
        }
    }
}

// Componentes extra reaproveitados (para não dar erro de ficheiro)
@Composable
fun TaskItemCardUser(title: String, time: String, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = Color(0xFF1C61A2), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = time, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun OverlappingAvatarsUser(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        visibleAvatars.forEach { url ->
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color(0xFFFFB74D)), contentAlignment = Alignment.Center) {
                if (!url.isNullOrEmpty() && url != "null") {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                }
            }
        }
        if (remaining > 0) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) { Text("+$remaining", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun CustomFilterChipUser(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(if (isSelected) Color(0xFF1C61A2) else Color.Transparent).clickable { onClick() }.padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}