package com.example.tp_loomo.ui.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.viewmodel.ManagerViewModel
import com.example.tp_loomo.viewmodel.ProfileViewModel
import com.example.tp_loomo.viewmodel.ProjectUiModel

@Composable
fun DashboardManagerScreen(
    onProjectClick: (Int) -> Unit = {},
    managerViewModel: ManagerViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        managerViewModel.loadDashboardData()
        profileViewModel.loadProfile()
    }

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
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                val primeiroNome = userData?.nomeCompleto?.split(" ")?.first() ?: stringResource(id = R.string.manager)
                Text(
                    text = stringResource(id = R.string.hello) + primeiroNome,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                // Reutiliza o teu "Bem-Vindo de Volta!"
                Text(text = stringResource(id = R.string.login_subtitle), fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Cartões de Estatísticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = stringResource(id = R.string.pending_tasks_stat),
                value = managerViewModel.totalPending.toString(),
                bgColor = Color(0xFF9EBAE1),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(id = R.string.completed_tasks_stat),
                value = managerViewModel.totalCompleted.toString(),
                bgColor = Color(0xFF90D992),
                textColor = Color(0xFF0AA20F),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Secção de Projetos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.my_projects),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = stringResource(id = R.string.see_all),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C61A2)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (managerViewModel.projectSummaries.isEmpty()) {
            Text(text = stringResource(id = R.string.no_projects_assigned), color = Color.Gray)
        } else {
            managerViewModel.projectSummaries.forEach { summary ->
                DashboardProjectCard(
                    uiModel = ProjectUiModel(
                        project = summary.project,
                        avatars = summary.avatars,
                        progress = if ((summary.pending + summary.completed) == 0) 0
                        else ((summary.completed.toFloat() / (summary.pending + summary.completed)) * 100).toInt(),
                        pendingTasks = summary.pending,
                        completedTasks = summary.completed
                    ),
                    onClick = { summary.project.id?.let { onProjectClick(it) } }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, bgColor: Color, textColor: Color = Color(0xFF1C61A2), modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = value, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
        }
    }
}

@Composable
fun DashboardProjectCard(uiModel: ProjectUiModel, onClick: () -> Unit) {
    val project = uiModel.project
    val avatars = uiModel.avatars

    var currentCover by remember(project.cover_url) {
        mutableStateOf<Any?>(
            when (project.cover_url) {
                "fundo_preto" -> com.example.tp_loomo.R.drawable.fundo_preto
                "fundo_rosa" -> com.example.tp_loomo.R.drawable.fundo_rosa
                "fundo_azul" -> com.example.tp_loomo.R.drawable.fundo_azul
                "fundo_branco" -> com.example.tp_loomo.R.drawable.fundo_branco
                else -> project.cover_url
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
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