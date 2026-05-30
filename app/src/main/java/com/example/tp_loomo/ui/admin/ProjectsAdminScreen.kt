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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Project
import com.example.tp_loomo.viewmodel.AdminViewModel
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

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

// Estruturas de dados para ler os membros da BD em segurança
@Serializable
data class CardMemberRow(val user_id: String)

// Atualiza a estrutura para ler também o nome!
@Serializable
data class CardProfileRow(val full_name: String? = null, val avatar_url: String? = null)

@Composable
fun AdminProjectListCard(project: Project, onClick: () -> Unit) {

    // --- ESTADOS 100% DINÂMICOS ---
    var managerName by remember { mutableStateOf("A carregar...") }
    var projectAvatars by remember { mutableStateOf<List<String?>>(emptyList()) }

    // --- DETETIVE (Vai buscar o Gestor E a Equipa) ---
    LaunchedEffect(project.id) {
        managerName = "A carregar..."
        projectAvatars = emptyList()

        try {
            // 1. Descobrir o Gestor
            var tempManagerAvatar: String? = null
            if (project.project_manager_id != null) {
                val managerProfile = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("full_name", "avatar_url")) {
                        filter { eq("id", project.project_manager_id) }
                    }.decodeSingleOrNull<CardProfileRow>()

                if (managerProfile != null) {
                    managerName = managerProfile.full_name ?: "Sem Nome"
                    tempManagerAvatar = managerProfile.avatar_url
                } else {
                    managerName = "Desconhecido"
                }
            } else {
                managerName = "Sem Gestor"
            }

            // 2. Descobrir a Equipa
            val members = supabase.postgrest["project_members"]
                .select(columns = Columns.list("user_id")) {
                    filter { eq("project_id", project.id) }
                }.decodeList<CardMemberRow>()

            val memberIds = members.map { it.user_id }
            var teamAvatars: List<String?> = emptyList()

            if (memberIds.isNotEmpty()) {
                val profiles = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("avatar_url")) {
                        filter { isIn("id", memberIds) }
                    }.decodeList<CardProfileRow>()

                teamAvatars = profiles.map { it.avatar_url }
            }

            // 3. Juntar tudo (Gestor em primeiro lugar, seguido da equipa)
            val combined = mutableListOf<String?>()
            combined.add(tempManagerAvatar) // Adiciona o gestor (mesmo que não tenha foto, adiciona null para desenhar o boneco default)
            combined.addAll(teamAvatars)

            // O "distinct()" remove fotos repetidas caso o gestor também esteja na tabela da equipa por engano
            projectAvatars = combined.distinct()

        } catch (e: Exception) {
            managerName = "Erro"
            projectAvatars = listOf(null) // Mostra um boneco de erro
        }
    }

    // --- LÓGICA DE CAPAS ---
    val savedCoverInDb = project.cover_url

    val finalCoverImageUrl: Any = when {
        savedCoverInDb == null -> {
            val covers = listOf(
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600",
                "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=600",
                "https://images.unsplash.com/photo-1550684376-efcbd6e3f031?q=80&w=600",
                "https://images.unsplash.com/photo-1614850523459-c2f4c699c52e?q=80&w=600"
            )
            covers[project.id % covers.size]
        }
        savedCoverInDb == "fundo_preto" -> R.drawable.fundo_preto
        savedCoverInDb == "fundo_branco" -> R.drawable.fundo_branco
        savedCoverInDb == "fundo_azul" -> R.drawable.fundo_azul
        savedCoverInDb == "fundo_rosa" -> R.drawable.fundo_rosa
        else -> savedCoverInDb
    }

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
            ) {
                AsyncImage(
                    model = finalCoverImageUrl,
                    contentDescription = "Capa do Projeto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 12.dp, end = 16.dp)
                ) {
                    // --- PASSA A LISTA COMPLETA E DINÂMICA ---
                    ProjectOverlappingAvatars(avatarUrls = projectAvatars)
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

                // --- AGORA O NOME É DINÂMICO ---
                Text(
                    text = "Gestor: $managerName",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
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
                    .background(Color(0xFFFFB74D)), // Cor de fundo padrão se não houver foto
                contentAlignment = Alignment.Center
            ) {
                // SE HOUVER UM LINK VÁLIDO, DESENHA A FOTO. SE NÃO, MOSTRA O BONECO BRANCO.
                if (!url.isNullOrBlank() && url.startsWith("http")) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Avatar do membro",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
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