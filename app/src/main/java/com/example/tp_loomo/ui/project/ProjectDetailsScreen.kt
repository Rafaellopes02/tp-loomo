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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.Task
import com.example.tp_loomo.ui.theme.DeadlinePillBackground
import com.example.tp_loomo.ui.theme.LoomoBlue
import com.example.tp_loomo.ui.theme.TaskCompletedGreen
import com.example.tp_loomo.ui.theme.TaskIconBackground
import com.example.tp_loomo.viewmodel.MainViewModel
import com.example.tp_loomo.viewmodel.ProjectDetailsViewModel
import com.example.tp_loomo.viewmodel.TasksViewModel
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    viewModel: ProjectDetailsViewModel = viewModel(),
    tasksViewModel: TasksViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Todas") }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf("") }

    // --- ESTADOS DA CAPA ---
    var showCoverScreen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    // --- ESTADOS DO MODO DE EDIÇÃO ---
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        mainViewModel.fetchUserRole()
    }

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
    }

    LaunchedEffect(projectId) {
        tasksViewModel.loadProjectTasks(projectId)
    }

    val project = viewModel.project
    val teamMembers = viewModel.teamMembers
    val isLoading = viewModel.isLoading
    val currentRole = mainViewModel.currentRole

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

    // RF12 — Filtrar tarefas conforme o tab selecionado
    val filteredTasks = when (selectedTab) {
        "Andamento" -> tasksViewModel.tasks.filter { it.status == "pending" || it.status == "in_progress" }
        "Concluído" -> tasksViewModel.tasks.filter { it.status == "completed" }
        else -> tasksViewModel.tasks
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // --- CABEÇALHO ---
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
                            model = currentCover,
                            contentDescription = "Capa do Projeto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.29f))
                    )

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
                                    onClick = {
                                        showMenu = false
                                        showCoverScreen = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Editar", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        editName = project.name
                                        editDescription = project.description ?: ""
                                        isEditing = true
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

                                DropdownMenuItem(
                                    text = { Text("Concluído", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    onClick = { showMenu = false }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F)) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // Bubble avatar do projeto (centro-baixo do header)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .size(68.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                            .background(brush = Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // TODO RF11: substituir por fetch real de perfis dos membros (avatarUrls ainda podem ser null)
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp)) {
                        OverlappingAvatars(avatarUrls = teamMembers.map { it.avatar_url })
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nome do Projeto") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Descrição") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateProject(projectId, editName, editDescription) {
                                        isEditing = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(text = project.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = project.description ?: "Sem descrição.", fontSize = 15.sp, color = Color.Gray, lineHeight = 22.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(50.dp)).background(DeadlinePillBackground).padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Prazo-Final: ${project.end_date ?: "Sem prazo"}",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(text = "50%", color = Color(0xFF1C61A2), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFE0E0E0))) {
                        Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(LoomoBlue))
                    }
                }
            }

            // --- TABS (FILTROS) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomFilterChip("Todas", selectedTab == "Todas") { selectedTab = "Todas" }
                    CustomFilterChip("Andamento", selectedTab == "Andamento") { selectedTab = "Andamento" }
                    CustomFilterChip("Concluído", selectedTab == "Concluído") { selectedTab = "Concluído" }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- LISTA DE TAREFAS (RF12) ---
            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_tasks),
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    TaskItemCard(
                        task = task,
                        onMarkCompleted = {
                            val taskId = task.id
                            if (taskId != null) {
                                tasksViewModel.markTaskAsCompleted(taskId, projectId)
                            }
                        }
                    )
                }
            }
        }

        // --- FAB — RF10 Criar Tarefa (só project_manager / manager) ---
        val role = mainViewModel.currentRole
        if (!showCoverScreen && (role == "manager" || role == "project_manager")) {
            FloatingActionButton(
                onClick = { showCreateTaskDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Color(0xFF1C61A2),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_task_dialog_title))
            }
        }

        // --- ECRÃ DE UPLOAD DE CAPA ---
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

                            supabase.postgrest["projects"].update(
                                { set("cover_url", finalUrlToSave) }
                            ) {
                                filter { eq("id", projectId) }
                            }

                            currentCover = newImage
                            showCoverScreen = false
                            Toast.makeText(context, "Capa guardada na BD!", Toast.LENGTH_SHORT).show()
                            viewModel.loadProjectDetails(projectId)

                        } catch (e: Exception) {
                            android.util.Log.e("ERRO_SUPABASE", "Falha ao gravar: ${e.message}", e)
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                }
            )
        }

        // --- MODAL DE CONFIRMAÇÃO DE ELIMINAÇÃO ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Eliminar Projeto", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black) },
                text = { Text(text = "Pretende mesmo eliminar este projeto? Esta ação é permanente e não poderá ser revertida.", fontSize = 15.sp, color = Color.DarkGray) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Sim", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }

        // --- MODAL DE CRIAR TAREFA (RF10) ---
        if (showCreateTaskDialog) {
            AlertDialog(
                onDismissRequest = { showCreateTaskDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.create_task_dialog_title),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text(stringResource(R.string.task_title_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newTaskDescription,
                            onValueChange = { newTaskDescription = it },
                            label = { Text(stringResource(R.string.task_description_optional)) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newTaskDueDate,
                            onValueChange = { newTaskDueDate = it },
                            label = { Text(stringResource(R.string.task_due_date_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskTitle.isBlank()) return@Button
                            tasksViewModel.createTask(
                                projectId = projectId,
                                title = newTaskTitle.trim(),
                                description = newTaskDescription.trim().ifBlank { null },
                                dueDate = newTaskDueDate.trim().ifBlank { null }
                            ) { success ->
                                if (success) {
                                    showCreateTaskDialog = false
                                    newTaskTitle = ""
                                    newTaskDescription = ""
                                    newTaskDueDate = ""
                                    Toast.makeText(context, context.getString(R.string.task_created_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.task_created_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.create), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTaskDialog = false }) {
                        Text(stringResource(R.string.btn_cancel), color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

// RF19 — Card de tarefa real com botão de concluir
@Composable
fun TaskItemCard(task: Task, onMarkCompleted: () -> Unit) {
    val isCompleted = task.status == "completed"
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TaskIconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = null,
                    tint = LoomoBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = task.due_date ?: stringResource(R.string.no_due_date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(
                onClick = { if (!isCompleted) onMarkCompleted() },
                enabled = !isCompleted
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.mark_task_completed),
                        tint = TaskCompletedGreen,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.mark_task_completed),
                        tint = Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
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
            ) {
                Text("+$remaining", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
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
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SetCoverScreen(
    onDismiss: () -> Unit,
    onSave: (Any) -> Unit
) {
    var selectedImage by remember { mutableStateOf<Any?>(null) }

    val systemBackgrounds = listOf(
        R.drawable.fundo_preto,
        R.drawable.fundo_rosa,
        R.drawable.fundo_branco,
        R.drawable.fundo_azul
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.Gray, modifier = Modifier.size(32.dp))
            }
            Text(text = "Set Cover", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

            TextButton(
                onClick = { selectedImage?.let { onSave(it) } },
                enabled = selectedImage != null
            ) {
                Text(
                    "Save",
                    color = if (selectedImage != null) Color(0xFF1C61A2) else Color.LightGray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Escolha uma imagem da sua galeria",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF9F9F9))
                    .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImage != null) {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = "Preview da Capa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Adicionar Foto",
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("+ Adicionar Foto", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Ou escolha um fundo do sistema",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                systemBackgrounds.forEach { drawableId ->
                    val isSelected = selectedImage == drawableId
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 4.dp else 1.dp,
                                color = if (isSelected) Color(0xFF1C61A2) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedImage = drawableId }
                    ) {
                        AsyncImage(
                            model = drawableId,
                            contentDescription = "Fundo do sistema",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
