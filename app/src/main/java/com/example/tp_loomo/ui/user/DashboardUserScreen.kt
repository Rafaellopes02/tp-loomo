package com.example.tp_loomo.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
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
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.utils.avatarDbValueToResource
import com.example.tp_loomo.viewmodel.UserViewModel

@Composable
fun DashboardUserScreen(
    onProjectClick: (Int) -> Unit = {},
    onTaskClick: (Int) -> Unit = {},
    onViewAllTasksClick: () -> Unit = {},
    viewModel: UserViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadUserDashboard()
    }

    val user = viewModel.currentUser
    val projects = viewModel.userProjects
    val tasks = viewModel.userTasks
    val isLoading = viewModel.isLoading

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // CABEÇALHO
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarResource = avatarDbValueToResource(user?.avatar_url)
                    if (avatarResource != null) {
                        AsyncImage(model = avatarResource, contentDescription = stringResource(id = R.string.porfile), modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.hello) + (user?.full_name?.split(" ")?.firstOrNull() ?: stringResource(id = R.string.user)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                    Text(text = stringResource(id = R.string.login_subtitle), fontSize = 14.sp, color = Color.Gray)
                }
            }
        }

        // CARROSSEL DE PROJETOS
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (projects.isEmpty()) {
                    item {
                        Text(stringResource(id = R.string.not_in_any_project), color = Color.Gray, modifier = Modifier.padding(vertical = 32.dp))
                    }
                } else {
                    items(projects) { project ->
                        ProjectCardUser(project = project, onClick = { project.id?.let { onProjectClick(it) } })
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // TÍTULO DAS TAREFAS
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.your_tasks_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(
                    text = stringResource(id = R.string.see_all),
                    fontSize = 14.sp,
                    color = Color(0xFF1C61A2),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onViewAllTasksClick() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // LISTA DE TAREFAS
        if (tasks.isEmpty()) {
            item {
                Text(stringResource(id = R.string.no_pending_tasks), color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            items(tasks.take(5)) { task ->
                TaskItemCard(
                    title = task.title,
                    time = task.due_date ?: stringResource(id = R.string.no_deadline),
                    onClick = {
                        task.id?.let { onTaskClick(it) }
                    }
                )
            }
        }
    }
}

// CARTÃO DE PROJETO
@Composable
fun ProjectCardUser(project: Project, onClick: () -> Unit) {
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
        modifier = Modifier.width(300.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp)
                    .background(Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8)))) // Fallback gradient
            ) {
                if (currentCover != null) {
                    AsyncImage(
                        model = currentCover,
                        contentDescription = stringResource(id = R.string.cover_content_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = project.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.project_type_label), fontSize = 13.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFEBEE)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(text = project.end_date ?: stringResource(id = R.string.no_deadline_short), color = Color(0xFFD32F2F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// CARTÃO DA TAREFA
@Composable
fun TaskItemCard(title: String, time: String, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
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