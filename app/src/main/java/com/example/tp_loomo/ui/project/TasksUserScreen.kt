package com.example.tp_loomo.ui.user

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.viewmodel.TaskUiModel
import com.example.tp_loomo.viewmodel.TasksUserViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TasksUserScreen(
    onTaskClick: (Int) -> Unit = {},
    viewModel: TasksUserViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("Todas") }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    val tasksList = viewModel.tasksList
    val isLoading = viewModel.isLoading

    // Filtra a lista com base no botão selecionado
    val filteredTasks = tasksList.filter {
        when (selectedFilter) {
            "Pendente" -> it.task.status == "pending" || it.task.status == null
            "Andamento" -> it.task.status == "in_progress" || it.task.completion_rate in 1..99
            else -> true // "Todas"
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
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp) // bottom alto por causa da navbar
    ) {
        // --- CABEÇALHO CENTRALIZADO ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "As Suas Tarefas", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Veja todas as suas tarefas", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- FILTROS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                FilterChipTask("Todas", selectedFilter == "Todas") { selectedFilter = "Todas" }
                Spacer(modifier = Modifier.width(12.dp))
                FilterChipTask("Pendente", selectedFilter == "Pendente") { selectedFilter = "Pendente" }
                Spacer(modifier = Modifier.width(12.dp))
                FilterChipTask("Andamento", selectedFilter == "Andamento") { selectedFilter = "Andamento" }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // --- LISTA DE CARTÕES GRANDES ---
        if (filteredTasks.isEmpty()) {
            item {
                Text(
                    text = "Sem tarefas para mostrar.",
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            items(filteredTasks) { item ->
                TaskLargeCard(
                    uiModel = item,
                    onClick = { item.task.id?.let { onTaskClick(it) } }
                )
            }
        }
    }
}

@Composable
fun TaskLargeCard(uiModel: TaskUiModel, onClick: () -> Unit) {
    val project = uiModel.project
    val task = uiModel.task
    val avatars = uiModel.avatars

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

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // --- PARTE DE CIMA: IMAGEM DE CAPA ---
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8))))
            ) {
                if (currentCover != null) {
                    AsyncImage(model = currentCover, contentDescription = "Capa", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))

                // Avatares no canto inferior direito
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp)) {
                    OverlappingAvatarsCard(avatarUrls = avatars)
                }
            }

            // --- PARTE DE BAIXO: TEXTOS E DATAS ---
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = project?.name ?: "Sem Projeto", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Mais", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = task.title + " - ", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

                    Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFEBEE)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = "Até ${formatDueDate(task.due_date)}", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// COMPONENTES AUXILIARES PARA ESTE ECRÃ
@Composable
fun FilterChipTask(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Color(0xFF1C61A2) else Color.White)
            .let {
                if (isSelected) it else it.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
            }
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// Formata a data "yyyy-MM-dd" vinda da BD para "dd Mmm yyyy" (ex: "16 Mai 2026")
fun formatDueDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "Indefinido"
    return try {
        val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val uiFormat = SimpleDateFormat("dd MMM yyyy", Locale("pt", "PT"))
        val date = dbFormat.parse(rawDate) ?: return rawDate
        uiFormat.format(date)
            .replace(".", "")
            .replaceFirstChar { it.uppercase() }
            .let { formatted ->
                // Capitaliza também o nome do mês (ex: "16 mai 2026" -> "16 Mai 2026")
                formatted.split(" ").joinToString(" ") { word ->
                    if (word.any { it.isLetter() }) word.replaceFirstChar { c -> c.uppercase() } else word
                }
            }
    } catch (e: Exception) {
        rawDate
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