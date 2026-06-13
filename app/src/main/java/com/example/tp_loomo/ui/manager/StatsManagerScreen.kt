package com.example.tp_loomo.ui.manager

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.ui.admin.stats.*
import com.example.tp_loomo.ui.admin.stats.reports.*
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun StatsManagerScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("Projetos") }
    var isLoading by remember { mutableStateOf(true) }

    // ID do gestor autenticado
    val currentUserId = remember { supabase.auth.currentUserOrNull()?.id ?: "" }

    var projectsList by remember { mutableStateOf<List<StatProject>>(emptyList()) }
    var tasksList by remember { mutableStateOf<List<StatTask>>(emptyList()) }
    var usersList by remember { mutableStateOf<List<StatUser>>(emptyList()) }
    var taskAssignmentsList by remember { mutableStateOf<List<StatTaskAssignment>>(emptyList()) }
    var membersConnections by remember { mutableStateOf<List<StatProjectMember>>(emptyList()) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // Projetos do Gestor
            projectsList = supabase.postgrest["projects"]
                .select(columns = Columns.list("id", "name", "project_manager_id", "description")) {
                    filter { eq("project_manager_id", currentUserId) }
                }
                .decodeList<StatProject>()
                .sortedByDescending { it.id }

            val projectIds = projectsList.map { it.id }

            // Se não for gestor de nenhum projeto
            if (projectIds.isEmpty()) {
                isLoading = false
                return@LaunchedEffect
            }

            // Membros apenas dos seus projetos
            membersConnections = supabase.postgrest["project_members"]
                .select(columns = Columns.list("project_id", "user_id")) {
                    filter { isIn("project_id", projectIds) }
                }
                .decodeList<StatProjectMember>()

            // IDs únicos dos utilizadores
            val memberUserIds = (
                    membersConnections.map { it.user_id } + projectsList.mapNotNull { it.project_manager_id }
                    ).distinct()

            if (memberUserIds.isNotEmpty()) {
                usersList = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("id", "full_name", "role", "username", "email", "avatar_url")) {
                        filter { isIn("id", memberUserIds) }
                    }
                    .decodeList<StatUser>()
            }

            // Tarefas apenas dos seus projetos
            tasksList = supabase.postgrest["tasks"]
                .select(columns = Columns.list(
                    "id", "project_id", "title", "description",
                    "due_date", "location", "status", "completion_rate",
                    "estimated_time", "actual_time", "notes"
                )) {
                    filter { isIn("project_id", projectIds) }
                }
                .decodeList<StatTask>()

            val taskIds = tasksList.map { it.id }

            // Assignments apenas das suas tarefas
            if (taskIds.isNotEmpty()) {
                taskAssignmentsList = supabase.postgrest["task_assignments"]
                    .select(columns = Columns.list("id", "task_id", "user_id")) {
                        filter { isIn("task_id", taskIds) }
                    }
                    .decodeList<StatTaskAssignment>()
            }

        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_load_stats, e.message ?: ""), Toast.LENGTH_LONG).show()
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
                text = stringResource(id = R.string.stats_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = stringResource(id = R.string.stats_subtitle),
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                StatsFilterChip(stringResource(id = R.string.tab_projects), isSelected = selectedTab == "Projetos") { selectedTab = "Projetos" }
                StatsFilterChip(stringResource(id = R.string.tab_tasks), isSelected = selectedTab == "Tarefas") { selectedTab = "Tarefas" }
                StatsFilterChip(stringResource(id = R.string.tab_users), isSelected = selectedTab == "Utilizadores") { selectedTab = "Utilizadores" }
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
                                item { CenterEmptyMessage(stringResource(id = R.string.no_projects_found)) }
                            } else {
                                items(projectsList) { proj ->
                                    val projectTasks = tasksList.filter { it.project_id == proj.id }
                                    val teamIds = membersConnections.filter { it.project_id == proj.id }.map { it.user_id }
                                    val allMembersCount = (teamIds + listOfNotNull(proj.project_manager_id)).distinct().size

                                    StatExportCard(
                                        title = proj.name,
                                        subtitle = stringResource(id = R.string.project_card_summary, projectTasks.size, allMembersCount),
                                        onDownloadClick = {
                                            coroutineScope.launch(Dispatchers.Main) {
                                                val manager = usersList.find { it.id == proj.project_manager_id }
                                                val team = usersList.filter { teamIds.contains(it.id) }

                                                val html = buildReportHtml(
                                                    context = context,
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
                            if (tasksList.isEmpty()) {
                                item { CenterEmptyMessage(stringResource(id = R.string.no_tasks_found)) }
                            } else {
                                items(tasksList) { task ->
                                    val projName = projectsList.find { it.id == task.project_id }?.name ?: stringResource(id = R.string.project_deleted)
                                    StatExportCard(
                                        title = task.title,
                                        subtitle = stringResource(id = R.string.project_prefix, projName),
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
                                                        context = context,
                                                        task = task,
                                                        projectName = projName,
                                                        assignedUsers = assigned,
                                                        records = records
                                                    )
                                                    exportTaskHtmlToPdf(context, html, task.title)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, context.getString(R.string.error_generic, e.message ?: ""), Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        "Utilizadores" -> {
                            if (usersList.isEmpty()) {
                                item { CenterEmptyMessage(stringResource(id = R.string.no_projects_found)) }
                            } else {
                                items(usersList) { user ->
                                    val roleLabel = when (user.role) {
                                        "admin" -> stringResource(id = R.string.admin)
                                        "project_manager" -> stringResource(id = R.string.project_manager_role)
                                        else -> stringResource(id = R.string.team_member_role)
                                    }
                                    StatExportCard(
                                        title = user.full_name ?: stringResource(id = R.string.unnamed_user),
                                        subtitle = stringResource(id = R.string.user_role_prefix, roleLabel),
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

                                                    val filteredRecords = records.filter { r ->
                                                        tasksList.any { it.id == r.task_id }
                                                    }

                                                    val html = buildUserReportHtml(
                                                        context = context,
                                                        user = user,
                                                        email = user.email,
                                                        avatarUrl = user.avatar_url,
                                                        memberProjects = memberProjects,
                                                        managedProjects = managedProjects,
                                                        assignedTasks = userTasks,
                                                        records = filteredRecords,
                                                        allTasks = tasksList
                                                    )
                                                    exportUserHtmlToPdf(context, html, user.full_name ?: "user")
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, context.getString(R.string.error_generic, e.message ?: ""), Toast.LENGTH_LONG).show()
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
}