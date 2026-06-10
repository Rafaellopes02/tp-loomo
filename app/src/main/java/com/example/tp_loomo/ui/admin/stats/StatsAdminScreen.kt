package com.example.tp_loomo.ui.admin.stats

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.ui.admin.stats.reports.StatTaskRecord2
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import com.example.tp_loomo.ui.admin.stats.reports.buildReportHtml  // <-- novo import
import com.example.tp_loomo.ui.admin.stats.reports.buildTaskReportHtml
import com.example.tp_loomo.ui.admin.stats.reports.buildUserReportHtml
import com.example.tp_loomo.ui.admin.stats.reports.exportHtmlToPdf
import com.example.tp_loomo.ui.admin.stats.reports.exportTaskHtmlToPdf
import com.example.tp_loomo.ui.admin.stats.reports.exportUserHtmlToPdf
import java.util.*


// -----------------------------------------------------------
// SCREEN PRINCIPAL
// -----------------------------------------------------------

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatsAdminScreenPreview() {
    // Dados fictícios
    val fakeProjects = listOf(
        StatProject(id = 1, name = "Projeto Alpha", project_manager_id = "u1", description = "Descrição do projeto Alpha"),
        StatProject(id = 2, name = "Projeto Beta", project_manager_id = "u2", description = "Descrição do projeto Beta")
    )
    val fakeTasks = listOf(
        StatTask(id = 1, project_id = 1, title = "Tarefa 1", status = "completed", due_date = "2026-06-10"),
        StatTask(id = 2, project_id = 1, title = "Tarefa 2", status = "pending", due_date = "2026-06-20"),
        StatTask(id = 3, project_id = 2, title = "Tarefa 3", status = "in_progress", due_date = "2026-07-01")
    )
    val fakeUsers = listOf(
        StatUser(id = "u1", full_name = "Rafael Lopes", role = "admin", username = "rafael.lopes"),
        StatUser(id = "u2", full_name = "Tiago Melo", role = "project_manager", username = "tiago.melo"),
        StatUser(id = "u3", full_name = "Pablo Mendes", role = "user", username = "pablo.mendes")
    )
    val fakeMembers = listOf(
        StatProjectMember(project_id = 1, user_id = "u2"),
        StatProjectMember(project_id = 1, user_id = "u3"),
        StatProjectMember(project_id = 2, user_id = "u3")
    )

    // Estado local para a preview
    var selectedTab by remember { mutableStateOf("Projetos") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Estatísticas",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = "Veja as estatísticas da app",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                StatsFilterChip("Projetos", isSelected = selectedTab == "Projetos") { selectedTab = "Projetos" }
                StatsFilterChip("Tarefas", isSelected = selectedTab == "Tarefas") { selectedTab = "Tarefas" }
                StatsFilterChip("Utilizadores", isSelected = selectedTab == "Utilizadores") { selectedTab = "Utilizadores" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                when (selectedTab) {
                    "Projetos" -> {
                        items(fakeProjects) { proj ->
                            val projectTasks = fakeTasks.filter { it.project_id == proj.id }
                            val teamIds = fakeMembers.filter { it.project_id == proj.id }.map { it.user_id }
                            val allMembersCount = (teamIds + listOfNotNull(proj.project_manager_id)).distinct().size
                            StatExportCard(
                                title = proj.name,
                                subtitle = "${projectTasks.size} Tarefas - $allMembersCount Membros",
                                onDownloadClick = {}
                            )
                        }
                    }
                    "Tarefas" -> {
                        items(fakeTasks) { task ->
                            val projName = fakeProjects.find { it.id == task.project_id }?.name ?: "Projeto Apagado"
                            StatExportCard(
                                title = task.title,
                                subtitle = "Projeto: $projName",
                                onDownloadClick = {}
                            )
                        }
                    }
                    "Utilizadores" -> {
                        items(fakeUsers) { user ->
                            val roleLabel = when (user.role) {
                                "admin" -> "Administrador"
                                "project_manager" -> "Gestor de Projeto"
                                else -> "Membro da Equipa"
                            }
                            StatExportCard(
                                title = user.full_name ?: "Sem Nome",
                                subtitle = "Cargo: $roleLabel",
                                onDownloadClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun StatsAdminScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("Projetos") }
    var isLoading by remember { mutableStateOf(true) }

    var projectsList by remember { mutableStateOf<List<StatProject>>(emptyList()) }
    var tasksList by remember { mutableStateOf<List<StatTask>>(emptyList()) }
    var usersList by remember { mutableStateOf<List<StatUser>>(emptyList()) }
    var taskAssignmentsList by remember { mutableStateOf<List<StatTaskAssignment>>(emptyList()) }
    var membersConnections by remember { mutableStateOf<List<StatProjectMember>>(emptyList()) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            projectsList = supabase.postgrest["projects"]
                .select(columns = Columns.list("id", "name", "project_manager_id", "description"))
                .decodeList<StatProject>()
                .sortedByDescending { it.id }

            tasksList = supabase.postgrest["tasks"]
                .select(columns = Columns.list(
                    "id", "project_id", "title", "description",
                    "due_date", "location", "status", "completion_rate",
                    "estimated_time", "actual_time", "notes"
                ))
                .decodeList<StatTask>()

            usersList = supabase.postgrest["profiles"]
                .select(columns = Columns.list("id", "full_name", "role", "username", "email", "avatar_url"))
                .decodeList<StatUser>()

            membersConnections = supabase.postgrest["project_members"]
                .select(columns = Columns.list("project_id", "user_id"))
                .decodeList<StatProjectMember>()

            taskAssignmentsList = supabase.postgrest["task_assignments"]
                .select(columns = Columns.list("id", "task_id", "user_id"))
                .decodeList<StatTaskAssignment>()

        } catch (e: Exception) {
            Toast.makeText(context, "Erro a carregar estatísticas: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Estatísticas",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            Text(
                text = "Veja as estatísticas da app",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                StatsFilterChip("Projetos", isSelected = selectedTab == "Projetos") { selectedTab = "Projetos" }
                StatsFilterChip("Tarefas", isSelected = selectedTab == "Tarefas") { selectedTab = "Tarefas" }
                StatsFilterChip("Utilizadores", isSelected = selectedTab == "Utilizadores") { selectedTab = "Utilizadores" }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1C61A2))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    when (selectedTab) {
                        "Projetos" -> {
                            if (projectsList.isEmpty()) {
                                item { CenterEmptyMessage("Nenhum projeto encontrado.") }
                            } else {
                                items(projectsList) { proj ->
                                    val projectTasks = tasksList.filter { it.project_id == proj.id }
                                    val teamIds = membersConnections.filter { it.project_id == proj.id }.map { it.user_id }
                                    val allMembersCount = (teamIds + listOfNotNull(proj.project_manager_id)).distinct().size

                                    StatExportCard(
                                        title = proj.name,
                                        subtitle = "${projectTasks.size} Tarefas - $allMembersCount Membros",
                                        onDownloadClick = {
                                            coroutineScope.launch(Dispatchers.Main) {
                                                val manager = usersList.find { it.id == proj.project_manager_id }
                                                val team = usersList.filter { teamIds.contains(it.id) }

                                                val html = buildReportHtml(
                                                    project = proj,
                                                    tasks = projectTasks,
                                                    manager = manager,
                                                    team = team,
                                                    taskAssignments = taskAssignmentsList,
                                                    allUsers = usersList
                                                )
                                                exportHtmlToPdf(context, html, proj.name)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        "Tarefas" -> {
                            items(tasksList) { task ->
                                val projName = projectsList.find { it.id == task.project_id }?.name ?: "Projeto Apagado"
                                StatExportCard(
                                    title = task.title,
                                    subtitle = "Projeto: $projName",
                                    onDownloadClick = {
                                        coroutineScope.launch(Dispatchers.Main) {
                                            try {
                                                val assigned = taskAssignmentsList
                                                    .filter { it.task_id == task.id }
                                                    .mapNotNull { a -> usersList.find { it.id == a.user_id } }

                                                val records = supabase.postgrest["task_records"]
                                                    .select(columns = Columns.list(
                                                        "id", "task_id", "user_id", "progress",
                                                        "location", "date", "time_spent", "observations", "photo_url"
                                                    )) {
                                                        filter { eq("task_id", task.id) }
                                                    }
                                                    .decodeList<StatTaskRecord2>()

                                                val html = buildTaskReportHtml(
                                                    task = task,
                                                    projectName = projName,
                                                    assignedUsers = assigned,
                                                    records = records
                                                )
                                                exportTaskHtmlToPdf(context, html, task.title)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        // No tab "Utilizadores":
                        "Utilizadores" -> {
                            items(usersList) { user ->
                                val roleLabel = when (user.role) {
                                    "admin" -> "Administrador"
                                    "project_manager" -> "Gestor de Projeto"
                                    else -> "Membro da Equipa"
                                }
                                StatExportCard(
                                    title = user.full_name ?: "Sem Nome",
                                    subtitle = "Cargo: $roleLabel",
                                    onDownloadClick = {
                                        coroutineScope.launch(Dispatchers.Main) {
                                            try {
                                                val userTaskIds = taskAssignmentsList
                                                    .filter { it.user_id == user.id }
                                                    .map { it.task_id }

                                                val userTasks = tasksList.filter { it.id in userTaskIds }

                                                val memberProjectIds = membersConnections
                                                    .filter { it.user_id == user.id }
                                                    .map { it.project_id }

                                                val memberProjects = projectsList.filter { it.id in memberProjectIds }
                                                val managedProjects = projectsList.filter { it.project_manager_id == user.id }

                                                val records = supabase.postgrest["task_records"]
                                                    .select(columns = Columns.list(
                                                        "id", "task_id", "user_id", "progress",
                                                        "location", "date", "time_spent", "observations", "photo_url"
                                                    )) {
                                                        filter { eq("user_id", user.id) }
                                                    }
                                                    .decodeList<StatTaskRecord2>()

                                                val html = buildUserReportHtml(
                                                    user = user,
                                                    email = user.email,
                                                    avatarUrl = user.avatar_url,
                                                    memberProjects = memberProjects,
                                                    managedProjects = managedProjects,
                                                    assignedTasks = userTasks,
                                                    records = records,
                                                    allTasks = tasksList
                                                )
                                                exportUserHtmlToPdf(context, html, user.full_name ?: "user")
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

