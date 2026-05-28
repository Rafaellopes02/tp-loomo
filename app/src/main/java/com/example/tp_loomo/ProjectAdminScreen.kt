package com.example.tp_loomo

import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ProjectDbModel(
    val id: Int,
    val name: String,
    val description: String? = null,
    val status: String? = null,
    val project_manager_id: String? = null,
    val cover_url: String? = null,
    val end_date: String? = null
)

@Serializable
data class ProjectMemberDbModel(
    val project_id: Int,
    val user_id: String
)

@Serializable
data class SimpleProfile(
    val id: String,
    val full_name: String? = null,
    val avatar_url: String? = null
)

@Serializable
data class TaskDbModel(
    val id: Int,
    val project_id: Int,
    val status: String? = null
)

data class ProjectUIModel(
    val id: String,
    val title: String,
    val description: String,
    val deadline: String,
    val managerName: String,
    val coverImageUrl: Any,
    val dbCoverUrl: String?, // NOVO: Guarda a foto original da BD
    val progress: Float,
    val pendingTasks: Int,
    val completedTasks: Int,
    val avatars: List<String?>
)

@Preview(showBackground = true)
@Composable
fun ProjectVisualCardPreview() {
    val dummyProject = ProjectUIModel(
        id = "1",
        title = "Trabalho Prático Redes",
        description = "Desenvolvimento de infraestrutura de redes",
        deadline = "2026-05-16",
        managerName = "Tiago Melo",
        coverImageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600",
        dbCoverUrl = null,
        progress = 0.75f,
        pendingTasks = 2,
        completedTasks = 6,
        avatars = listOf(null, null, null, null)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp)
    ) {
        ProjectVisualCard(project = dummyProject, onClick = {})
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProjectsAdminScreenPreview() {
    ProjectsAdminScreen(navController = rememberNavController())
}

@Composable
fun ProjectsAdminScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf("Todas") }
    var projects by remember { mutableStateOf<List<ProjectUIModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchProjects() {
        coroutineScope.launch {
            isLoading = true
            try {
                val dbProjects = supabase.postgrest["projects"]
                    .select(columns = Columns.list("id", "name", "description", "status", "project_manager_id","cover_url", "end_date"))
                    .decodeList<ProjectDbModel>()
                    .sortedBy { it.id }

                val allProfiles = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("id", "full_name", "avatar_url"))
                    .decodeList<SimpleProfile>()

                val allMembers = supabase.postgrest["project_members"]
                    .select()
                    .decodeList<ProjectMemberDbModel>()

                val allTasks = supabase.postgrest["tasks"]
                    .select(columns = Columns.list("id", "project_id", "status"))
                    .decodeList<TaskDbModel>()

                val mappedProjects = dbProjects.map { proj ->
                    val manager = allProfiles.find { it.id == proj.project_manager_id }
                    val managerName = manager?.full_name ?: "Gestor não atribuído"

                    val memberIds = allMembers.filter { it.project_id == proj.id }.map { it.user_id }
                    val teamAvatars = allProfiles.filter { it.id in memberIds }.map { it.avatar_url }
                    val allAvatars = (listOf(manager?.avatar_url) + teamAvatars).filterNotNull().distinct()

                    val projectTasks = allTasks.filter { it.project_id == proj.id }
                    val completed = projectTasks.count { task ->
                        val s = task.status?.lowercase() ?: ""
                        s.contains("conclui") || s.contains("complet") || s.contains("done")
                    }
                    val pending = projectTasks.size - completed
                    val realProgress = if (projectTasks.isEmpty()) 0f else (completed.toFloat() / projectTasks.size.toFloat())

                    // --- NOVA LÓGICA DE CAPA CORRIGIDA ---
                    // Lemos o valor que está na coluna 'cover_url' da BD
                    val savedCoverInDb = proj.cover_url

                    // Definimos a imagem real a mostrar no cartão
                    val finalCoverImageUrl: Any = when {
                        // 1. Se estiver vazio na BD, usamos a lógica aleatória antiga
                        savedCoverInDb == null -> {
                            val covers = listOf(
                                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600",
                                "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=600",
                                "https://images.unsplash.com/photo-1550684376-efcbd6e3f031?q=80&w=600",
                                "https://images.unsplash.com/photo-1614850523459-c2f4c699c52e?q=80&w=600"
                            )
                            covers[proj.id % covers.size]
                        }
                        // 2. Se for um fundo do sistema, convertemos o TEXTO no DESENHO real
                        savedCoverInDb == "fundo_preto" -> R.drawable.fundo_preto
                        savedCoverInDb == "fundo_branco"-> R.drawable.fundo_branco
                        savedCoverInDb == "fundo_rosa" -> R.drawable.fundo_rosa
                        // 3. Caso contrário, é o link HTTPS (da galeria ou Unsplash custom)
                        else -> savedCoverInDb
                    }

                    ProjectUIModel(
                        id = proj.id.toString(),
                        title = proj.name,
                        description = proj.description ?: "Sem descrição disponível.",
                        deadline = proj.end_date ?: "Sem prazo definido",
                        managerName = managerName,
                        coverImageUrl = finalCoverImageUrl, // Passamos a imagem real (Recurso ou Link)
                        dbCoverUrl = proj.cover_url, // Guardamos o original para navegação
                        progress = realProgress,
                        pendingTasks = pending,
                        completedTasks = completed,
                        avatars = allAvatars.ifEmpty { listOf(null) }
                    )
                }
                projects = mappedProjects
            } catch (e: Exception) {
                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // 2. Em vez do 'Unit', escutamos o 'navBackStackEntry'
    // Assim, SEMPRE que voltares a este ecrã, ele saca os dados fresquinhos da BD!
    LaunchedEffect(navBackStackEntry) {
        fetchProjects()
    }

    val filteredProjects = when (selectedFilter) {
        "Andamento" -> projects.filter { it.progress < 1f }
        "Concluidos" -> projects.filter { it.progress >= 1f }
        else -> projects
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Todos Os Projetos",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = "Veja todos os projetos da app",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                FilterChipCustom("Todas", isSelected = selectedFilter == "Todas") { selectedFilter = "Todas" }
                FilterChipCustom("Andamento", isSelected = selectedFilter == "Andamento") { selectedFilter = "Andamento" }
                FilterChipCustom("Concluidos", isSelected = selectedFilter == "Concluidos") { selectedFilter = "Concluidos" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1C61A2))
                }
            } else if (filteredProjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum projeto encontrado.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredProjects) { project ->
                        ProjectVisualCard(
                            project = project,
                            onClick = {
                                val safeTitle = project.title.trim().replace(" ", "_").replace("/", "-").ifBlank { "Sem_Titulo" }
                                val safeDesc = project.description.trim().replace(" ", "_").replace("/", "-").take(60).ifBlank { "Sem_desc" }
                                val safeDeadline = project.deadline.trim().replace(" ", "_").replace("/", "-").ifBlank { "Sem_prazo" }
                                val safeFotosUrl = project.dbCoverUrl?.let { java.net.URLEncoder.encode(it, "UTF-8") } ?: "null"

                                // MAGIA AQUI: Transforma as fotos todas num código 100% seguro (Base64)
                                val avatarsJoined = project.avatars.filterNotNull().joinToString("|")
                                val safeAvatars = if (avatarsJoined.isNotBlank()) {
                                    android.util.Base64.encodeToString(avatarsJoined.toByteArray(Charsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                                } else {
                                    "null"
                                }

                                // NAVEGAÇÃO
                                navController.navigate("projectDetails/${project.id}/$safeTitle/$safeDesc/$safeDeadline?fotos_url=$safeFotosUrl&avatars=$safeAvatars")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectVisualCard(project: ProjectUIModel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = project.coverImageUrl,
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
                        .padding(bottom = 8.dp, end = 16.dp)
                ) {
                    OverlappingAvatarsSmall(avatarUrls = project.avatars)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Opções",
                        tint = Color(0xFF1C61A2)
                    )
                }

                Text(
                    text = "Gestor: ${project.managerName}",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(project.progress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C61A2),
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = project.progress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Color(0xFF1C61A2))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${project.pendingTasks} tarefas pendentes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C61A2)
                    )
                    Text(
                        text = "${project.completedTasks} concluídas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF17B83A)
                    )
                }
            }
        }
    }
}

@Composable
fun OverlappingAvatarsSmall(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
        visibleAvatars.forEach { url ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White, CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
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
                    .size(30.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.White, CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}