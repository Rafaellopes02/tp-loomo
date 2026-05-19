package com.example.tp_loomo.ui.user

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.viewmodel.ProfileViewModel
import com.example.tp_loomo.viewmodel.UserViewModel

@Composable
fun DashboardUserScreen(
    userViewModel: UserViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Carregar dados de perfil e projetos ao iniciar
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
        userViewModel.loadDashboardData()
    }

    val userData = profileViewModel.userData
    val projects = userViewModel.userProjects

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Cabeçalho Real
        DashboardHeader(
            nome = userData?.nomeCompleto?.split(" ")?.first() ?: "Utilizador",
            avatarUrl = userData?.avatarUrl
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Destaque: Mostra o primeiro projeto da lista, se existir
        if (projects.isNotEmpty()) {
            ProjectHighlightCard(projects.first())
        } else if (userViewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Text(text = "Ainda não tens projetos atribuídos.", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Os Teus Projetos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de projetos do utilizador
        projects.forEach { project ->
            TaskCard(title = project.name, project = project.status ?: "Ativo")
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun DashboardHeader(nome: String, avatarUrl: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "${stringResource(id = R.string.hello)} $nome", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = stringResource(id = R.string.login_subtitle), fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ProjectHighlightCard(project: Project) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF), Color(0xFF1C61A2))))
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = project.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color(0xFF1C61A2))
                }
                Spacer(modifier = Modifier.height(8.dp))
                project.end_date?.let {
                    Text(text = "Prazo: $it", fontSize = 12.sp, color = Color.Gray)
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
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.FormatListBulleted, contentDescription = null, tint = Color(0xFF1C61A2), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Status: $project", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}