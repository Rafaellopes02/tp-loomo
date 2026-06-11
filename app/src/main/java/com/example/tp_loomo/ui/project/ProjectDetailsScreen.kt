package com.example.tp_loomo.ui.project

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.UserProfile
import com.example.tp_loomo.ui.admin.CardProfileRow
import com.example.tp_loomo.viewmodel.ProjectDetailsViewModel
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    onTaskClick: (taskId: Int) -> Unit = {},
    viewModel: ProjectDetailsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Todas") }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateTaskModal by remember { mutableStateOf(false) }
    var showCoverScreen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    var showEvaluateModal by remember { mutableStateOf(false) }
    var realManagerName by remember { mutableStateOf("A carregar...") }
    var realManagerAvatar by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
    }

    val project = viewModel.project
    val teamMembers = viewModel.teamMembers
    val projectTasks = viewModel.projectTasks
    val isLoading = viewModel.isLoading
    val allUsers = viewModel.allUsers

    // Cálculo dinâmico da percentagem
    val totalTasks = projectTasks.size
    val completedTasks = projectTasks.count { it.status == "completed" || it.completion_rate == 100 }
    val progressFloat = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    val progressPercent = (progressFloat * 100).toInt()

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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(bottom = 100.dp)
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
                        AsyncImage(model = currentCover, contentDescription = "Capa do Projeto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Mais", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mudar fundo", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Black) },
                                    onClick = { showMenu = false; showCoverScreen = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Editar", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Black) },
                                    onClick = { showMenu = false; editName = project.name; editDescription = project.description ?: ""; isEditing = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("Concluir Projeto", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF388E3C)) },
                                    onClick = { showMenu = false; showEvaluateModal = true }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F)) },
                                    onClick = { showMenu = false; showDeleteDialog = true }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp)) {
                        OverlappingAvatars(avatarUrls = projectAvatars)
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    if (isEditing) {
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Nome do Projeto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditing = false }) { Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.updateProject(projectId, editName, editDescription) { isEditing = false } },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)), shape = RoundedCornerShape(12.dp)
                            ) { Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Text(text = project.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = project.description ?: "Sem descrição.", fontSize = 15.sp, color = Color.Gray, lineHeight = 22.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Gestor: $realManagerName", fontSize = 15.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEBEE)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(text = "Prazo-Final: ${project.end_date ?: "Sem prazo"}", color = Color(0xFFD32F2F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        // CORRIGIDO: percentagem dinâmica
                        Text(text = "$progressPercent%", color = Color(0xFF1C61A2), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE0E0E0))) {
                        // CORRIGIDO: barra dinâmica
                        Box(modifier = Modifier.fillMaxWidth(progressFloat).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Color(0xFF1C61A2)))
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    CustomFilterChip("Todas", selectedTab == "Todas") { selectedTab = "Todas" }
                    CustomFilterChip("Andamento", selectedTab == "Andamento") { selectedTab = "Andamento" }
                    CustomFilterChip("Concluído", selectedTab == "Concluído") { selectedTab = "Concluído" }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (projectTasks.isEmpty()) {
                item {
                    Text("Sem tarefas para apresentar.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                items(projectTasks) { task ->
                    TaskItemCard(
                        title = task.title,
                        time = task.due_date ?: "Sem data",
                        onClick = { task.id?.let { onTaskClick(it) } }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateTaskModal = true },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            containerColor = Color(0xFF1C61A2),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova Tarefa", modifier = Modifier.size(32.dp))
        }

        if (showCreateTaskModal) {
            CreateTaskBottomSheet(
                teamMembers = allUsers,
                onDismiss = { showCreateTaskModal = false },
                onSave = { titulo, desc, dueDate, membrosSelecionadosIds ->
                    viewModel.createTask(projectId, titulo, desc, dueDate, membrosSelecionadosIds) {
                        showCreateTaskModal = false
                    }
                }
            )
        }

        if (showCoverScreen) {
            SetCoverScreen(
                onDismiss = { showCoverScreen = false },
                onSave = { newImage ->
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            val finalUrlToSave: String = when {
                                newImage == R.drawable.fundo_preto -> "fundo_preto"
                                newImage == R.drawable.fundo_branco -> "fundo_branco"
                                newImage == R.drawable.fundo_azul -> "fundo_azul"
                                newImage == R.drawable.fundo_rosa -> "fundo_rosa"
                                newImage.toString().startsWith("content://") -> {
                                    val uri = android.net.Uri.parse(newImage.toString())
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val byteArray = inputStream?.readBytes() ?: throw Exception("Erro a ler foto")
                                    val fileName = "projeto_${projectId}_${System.currentTimeMillis()}.jpg"
                                    val bucket = supabase.storage["covers"]
                                    bucket.upload(fileName, byteArray)
                                    bucket.publicUrl(fileName)
                                }
                                else -> newImage.toString()
                            }
                            supabase.postgrest["projects"].update({ set("cover_url", finalUrlToSave) }) { filter { eq("id", projectId) } }
                            currentCover = newImage
                            showCoverScreen = false
                            Toast.makeText(context, "Capa guardada na BD!", Toast.LENGTH_SHORT).show()
                            viewModel.loadProjectDetails(projectId)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Eliminar Projeto", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black) },
                text = { Text(text = "Pretende mesmo eliminar este projeto?", fontSize = 15.sp, color = Color.DarkGray) },
                confirmButton = {
                    Button(colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)), shape = RoundedCornerShape(12.dp), onClick = { showDeleteDialog = false }) { Text("Sim", color = Color.White, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = Color.Gray) }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
        if (showEvaluateModal) {
            val taskCountByMember = remember(projectTasks, teamMembers) {
                teamMembers.associate { member ->
                    member.id to projectTasks.count { task ->
                        task.status == "completed" || task.completion_rate == 100
                    }
                }
            }
            EvaluateTeamBottomSheet(
                teamMembers = teamMembers,
                taskCounts  = taskCountByMember,
                onDismiss   = { showEvaluateModal = false },
                onConfirm   = { evaluations ->
                    viewModel.submitTeamEvaluations(projectId, evaluations) {
                        showEvaluateModal = false
                        onBackClick()
                    }
                }
            )
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskBottomSheet(
    teamMembers: List<UserProfile>,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, dueDate: String?, selectedMemberIds: List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    var expandedMenu by remember { mutableStateOf(false) }

    var selectedMembers by remember { mutableStateOf(setOf<UserProfile>()) }

    val uiDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(text = "Criar nova tarefa", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titulo da tarefa") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(24.dp))

            // --- DATA ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Prazo-Limite", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }
                Button(
                    onClick = { showDatePicker = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5B84)),
                    shape = RoundedCornerShape(20.dp), modifier = Modifier.height(36.dp)
                ) { Text(if (selectedDateMillis == null) "Adicionar" else "Alterar", color = Color.White, fontSize = 13.sp) }
            }
            if (selectedDateMillis != null) {
                Text(text = uiDateFormat.format(Date(selectedDateMillis!!)), color = Color(0xFF1C61A2), fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 32.dp, top = 4.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- EQUIPA MULTI-SELEÇÃO ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.People, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Equipa", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }
                Box {
                    Button(
                        onClick = { expandedMenu = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5B84)),
                        shape = RoundedCornerShape(20.dp), modifier = Modifier.height(36.dp)
                    ) { Text(if (selectedMembers.isEmpty()) "Adicionar" else "Alterar", color = Color.White, fontSize = 13.sp) }

                    DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                        if (teamMembers.isEmpty()) {
                            DropdownMenuItem(text = { Text("Sem membros no projeto") }, onClick = { expandedMenu = false })
                        } else {
                            teamMembers.forEach { member ->
                                val isSelected = selectedMembers.contains(member)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1C61A2))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(member.full_name ?: member.username ?: "Utilizador")
                                        }
                                    },
                                    onClick = {
                                        selectedMembers = if (isSelected) {
                                            selectedMembers - member
                                        } else {
                                            selectedMembers + member
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedMembers.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(start = 32.dp, top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OverlappingAvatars(avatarUrls = selectedMembers.map { it.avatar_url }.toList())
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "- ${selectedMembers.size} Membros",
                        color = Color(0xFF1C61A2),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val dbDate = selectedDateMillis?.let { dbDateFormat.format(Date(it)) }
                    onSave(title, description, dbDate, selectedMembers.map { it.id }.toList())
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B5B84)),
                shape = RoundedCornerShape(16.dp)
            ) { Text("Concluído", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { selectedDateMillis = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun OverlappingAvatars(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        visibleAvatars.forEach { url ->
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color(0xFFFFB74D)),
                contentAlignment = Alignment.Center
            ) {
                if (!url.isNullOrEmpty() && url != "null") {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                }
            }
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) { Text("+$remaining", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun CustomFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Color(0xFF1C61A2) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SetCoverScreen(
    onDismiss: () -> Unit,
    onSave: (Any) -> Unit
) {
    var selectedImage by remember { mutableStateOf<Any?>(null) }
    val systemBackgrounds = listOf(R.drawable.fundo_preto, R.drawable.fundo_rosa, R.drawable.fundo_branco, R.drawable.fundo_azul)
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) selectedImage = uri.toString() }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.Gray, modifier = Modifier.size(32.dp)) }
            Text(text = "Set Cover", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            TextButton(onClick = { selectedImage?.let { onSave(it) } }, enabled = selectedImage != null) { Text("Save", color = if (selectedImage != null) Color(0xFF1C61A2) else Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Escolha uma imagem da sua galeria", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF9F9F9)).border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)).clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImage != null) AsyncImage(model = selectedImage, contentDescription = "Preview da Capa", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Outlined.Image, contentDescription = "Adicionar Foto", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("+ Adicionar Foto", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(text = "Ou escolha um fundo do sistema", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
                systemBackgrounds.forEach { drawableId ->
                    val isSelected = selectedImage == drawableId
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).border(width = if (isSelected) 4.dp else 1.dp, color = if (isSelected) Color(0xFF1C61A2) else Color(0xFFE0E0E0), shape = RoundedCornerShape(12.dp)).clickable { selectedImage = drawableId }) {
                        AsyncImage(model = drawableId, contentDescription = "Fundo do sistema", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
            }
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluateTeamBottomSheet(
    teamMembers: List<UserProfile>,
    taskCounts: Map<String, Int>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Pair<Int, String>>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ratings      = remember { mutableStateMapOf<String, Int>() }
    val observations = remember { mutableStateMapOf<String, String>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)  // ocupa 90% do ecrã
        ) {
            // Título fixo no topo
            Text(
                text = "Avaliar Equipa",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 20.dp)
            )

            // Lista com scroll
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(teamMembers) { member ->
                    MemberEvaluationCard(
                        member         = member,
                        completedTasks = taskCounts[member.id] ?: 0,
                        rating         = ratings[member.id] ?: 0,
                        observation    = observations[member.id] ?: "",
                        onRatingChange = { ratings[member.id] = it },
                        onObsChange    = { observations[member.id] = it }
                    )
                }
            }

            // Botão fixo no fundo
            Button(
                onClick = {
                    val result = teamMembers.associate { m ->
                        m.id to Pair(ratings[m.id] ?: 0, observations[m.id] ?: "")
                    }
                    onConfirm(result)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Concluído", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MemberEvaluationCard(
    member: UserProfile,
    completedTasks: Int,
    rating: Int,
    observation: String,
    onRatingChange: (Int) -> Unit,
    onObsChange: (String) -> Unit
) {
    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Avatar + nome + tarefas
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB74D)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!member.avatar_url.isNullOrEmpty() && member.avatar_url != "null") {
                        AsyncImage(
                            model = member.avatar_url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = member.full_name ?: member.username ?: "Utilizador",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "$completedTasks tarefas concluídas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Estrelas
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= rating)
                            androidx.compose.material.icons.Icons.Filled.Star
                        else
                            androidx.compose.material.icons.Icons.Outlined.StarBorder,
                        contentDescription = "Estrela $star",
                        tint = if (star <= rating) Color(0xFFFFC107) else Color(0xFFCCCCCC),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onRatingChange(star) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de observações
            OutlinedTextField(
                value = observation,
                onValueChange = onObsChange,
                placeholder = { Text("Observações ...", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color(0xFF1C61A2),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }
    }
}