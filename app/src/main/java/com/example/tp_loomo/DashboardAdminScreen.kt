package com.example.tp_loomo

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // IMPORT ADICIONADO AQUI
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen(
    navController: NavController // PARAMETRO ATUALIZADO AQUI
) {
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var nome by remember { mutableStateOf("Admin") }
    val coroutineScope = rememberCoroutineScope()
    var showAddUserModal by remember { mutableStateOf(false) }
    var showAddProjectModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    avatarUrl = currentUser.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
                    val fullName = currentUser.userMetadata?.get("full_name")?.toString()?.replace("\"", "")

                    if (!fullName.isNullOrBlank() && fullName != "null") {
                        nome = fullName.split(" ").first()
                    }
                }
            } catch (e: Exception) { }
        }
    }

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
                if (!avatarUrl.isNullOrEmpty() && avatarUrl != "null") {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "${stringResource(id = R.string.hello)} $nome",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(text = "Administrador", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Grelha de Estatísticas
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(title = stringResource(id = R.string.totalProjects), value = "18", bgColor = Color(0xFF9EBAE1), titleColor = Color(0xFF1C61A2), modifier = Modifier.weight(1f))
                AdminStatCard(title = stringResource(id = R.string.users), value = "3", bgColor = Color(0xFFC4AED6), titleColor = Color(0xFF6A1B9A), modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(title = stringResource(id = R.string.activeTasks), value = "8", bgColor = Color(0xFFF3E19C), titleColor = Color(0xFFF57F17), modifier = Modifier.weight(1f))
                AdminStatCard(title = stringResource(id = R.string.completedProjects), value = "2", bgColor = Color(0xFF90D992), titleColor = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = stringResource(R.string.quickActions), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))

        AdminActionCard(
            icon = Icons.Outlined.Add,
            title = stringResource(R.string.createNewProject),
            subtitle = stringResource(id = R.string.clickToCreateNewProject),
            onClick = { showAddProjectModal = true }
        )
        Spacer(modifier = Modifier.height(12.dp))

        AdminActionCard(
            icon = Icons.Outlined.PersonAdd,
            title = stringResource(id = R.string.addUser),
            subtitle = stringResource(id = R.string.manageAddNewUser),
            onClick = { showAddUserModal = true }
        )

        Spacer(modifier = Modifier.height(12.dp))
        AdminActionCard(icon = Icons.Outlined.TrendingUp, title = stringResource(id = R.string.exportStatistics), subtitle = stringResource(id = R.string.exportReports))

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddUserModal) {
        AddUserBottomSheet(onDismiss = { showAddUserModal = false })
    }

    if (showAddProjectModal) {
        AddProjectBottomSheet(
            onDismiss = { showAddProjectModal = false },
            navController = navController // PASSADO AQUI
        )
    }
}

// Enum para sabermos qual modal abrir
enum class SelectionType { MANAGER, TEAM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectBottomSheet(
    onDismiss: () -> Unit,
    navController: NavController // PARAMETRO ATUALIZADO AQUI
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Calendar?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Estado para o Project Manager (Apenas 1)
    var selectedManager by remember { mutableStateOf<UserProfile?>(null) }

    // Estado para a Equipa (Vários)
    val selectedUsers = remember { mutableStateListOf<UserProfile>() }

    // Controlo de Modais
    var selectionType by remember { mutableStateOf<SelectionType?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.createNewProject), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(24.dp))

            // Campo: Título
            AddUserTextField(label = stringResource(id = R.string.projectTitle), value = title, onValueChange = { title = it })
            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Descrição
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.description), fontSize = 14.sp, color = Color(0xFF4A4A4A))
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F3F3),
                        unfocusedContainerColor = Color(0xFFF3F3F3),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- SECÇÃO: PROJECT MANAGER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Project Manager", fontSize = 16.sp, color = Color(0xFF4A4A4A))
                }
                Button(
                    onClick = { selectionType = SelectionType.MANAGER },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Avatar do Manager selecionado
            if (selectedManager != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    AsyncImage(
                        model = selectedManager?.avatar_url,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray).clickable {
                            selectedManager = null
                        },
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- SECÇÃO: EQUIPA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Team", fontSize = 16.sp, color = Color(0xFF4A4A4A))
                }
                Button(
                    onClick = { selectionType = SelectionType.TEAM },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Avatars dos selecionados (Equipa)
            if (selectedUsers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy((-10).dp)
                ) {
                    selectedUsers.forEach { user ->
                        AsyncImage(
                            model = user.avatar_url,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray).clickable {
                                selectedUsers.remove(user)
                            },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Prazo-Limite
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.projectDeadline), fontSize = 16.sp, color = Color.Gray)
                    deadline?.let {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        Text(text = sdf.format(it.time), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C61A2))
                    }
                }
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selected = Calendar.getInstance()
                                selected.set(year, month, dayOfMonth)
                                deadline = selected
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(id = R.string.add), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Criar
            Button(
                onClick = {
                    if (isLoading) return@Button

                    if (title.isBlank() || selectedManager == null) {
                        Toast.makeText(context, "Preencha o título e selecione um Manager", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val startDate = sdf.format(Date())
                            val endDate = deadline?.let { sdf.format(it.time) }

                            // 1. Criar o Projeto
                            val projectData = buildJsonObject {
                                put("name", title.trim())
                                put("description", description.trim())
                                put("start_date", startDate)
                                if (endDate != null) put("end_date", endDate)
                                put("project_manager_id", selectedManager!!.id)
                                put("status", "active")
                            }

                            val response = supabase.postgrest["projects"]
                                .insert(projectData) { select() }
                                .decodeSingle<JsonObject>()

                            val projectId = response["id"]?.toString()?.toIntOrNull()

                            // 2. Inserir a Equipa
                            if (projectId != null && selectedUsers.isNotEmpty()) {
                                val membersData = selectedUsers.map { user ->
                                    buildJsonObject {
                                        put("project_id", projectId)
                                        put("user_id", user.id)
                                    }
                                }
                                supabase.postgrest["project_members"].insert(membersData)
                            }

                            // Formata a data para a Rota (URL)
                            val dateForScreen = deadline?.let {
                                SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(it.time)
                            } ?: "Sem_prazo"

                            // Limpa espaços no titulo e descrição para não estragar a Rota
                            val safeTitle = title.trim().replace(" ", "_").replace("/", "-").ifBlank { "Sem_Titulo" }
                            val safeDesc = if (description.isNotBlank()) description.trim().replace(" ", "_").replace("/", "-").take(50) else "Sem_desc"

                            Toast.makeText(context, "Projeto criado com sucesso!", Toast.LENGTH_SHORT).show()
                            onDismiss()

                            // Dispara a navegação!
                            navController.navigate("projectDetails/$safeTitle/$safeDesc/$dateForScreen")

                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro BD: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(id = R.string.create), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (selectionType != null) {
        UserSelectionDialog(
            type = selectionType!!,
            onDismiss = { selectionType = null },
            onUserSelected = { user ->
                if (selectionType == SelectionType.MANAGER) {
                    selectedManager = user
                } else {
                    if (!selectedUsers.any { it.id == user.id }) selectedUsers.add(user)
                }
                selectionType = null
            }
        )
    }
}

@Composable
fun UserSelectionDialog(type: SelectionType, onDismiss: () -> Unit, onUserSelected: (UserProfile) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(type) {
        try {
            val roleFilter = if (type == SelectionType.MANAGER) "project_manager" else "user"

            users = supabase.postgrest["profiles"].select {
                filter {
                    eq("role", roleFilter)
                }
            }.decodeList<UserProfile>()

        } catch (e: Exception) {
            errorMessage = e.localizedMessage
        } finally {
            isLoading = false
        }
    }

    val filteredUsers = users.filter {
        val nameMatch = it.full_name?.contains(searchQuery, ignoreCase = true) == true
        val userMatch = it.username?.contains(searchQuery, ignoreCase = true) == true
        nameMatch || userMatch
    }

    val modalTitle = if (type == SelectionType.MANAGER) "Selecionar Gestor" else "Selecionar Membro"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
        title = { Text(modalTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Pesquisar...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (errorMessage != null) {
                    Text("Erro: $errorMessage", color = Color.Red, fontSize = 12.sp)
                } else if (users.isEmpty()) {
                    Text("Nenhum utilizador encontrado com este cargo.", fontSize = 14.sp, color = Color.Gray)
                } else {
                    LazyColumn {
                        items(filteredUsers) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserSelected(user) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatar_url,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.full_name ?: "Sem nome", fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text("@${user.username ?: "user"}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AdminActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1C61A2), modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, bgColor: Color, titleColor: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(text = value, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserBottomSheet(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
    var isLoading by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.createNewUser), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(24.dp))

            AddUserTextField(label = stringResource(id = R.string.full_name), value = fullName, onValueChange = { fullName = it })
            Spacer(modifier = Modifier.height(16.dp))

            AddUserTextField(label = stringResource(id = R.string.username), value = username, onValueChange = { username = it })
            Spacer(modifier = Modifier.height(16.dp))

            AddUserTextField(label = stringResource(id = R.string.email), value = email, onValueChange = { email = it })
            Spacer(modifier = Modifier.height(16.dp))

            RoleDropdown(selectedRole = role, onRoleSelected = { role = it })

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            supabase.auth.signUpWith(Email) {
                                this.email = email.trim()
                                this.password = "loomo26"
                                data = buildJsonObject {
                                    put("full_name", fullName.trim())
                                    put("username", username.trim())
                                    put("role", role)
                                }
                            }
                            val newUserId = supabase.auth.currentUserOrNull()?.id
                            if (newUserId != null) {
                                val updateData = buildJsonObject {
                                    put("role", role)
                                    put("email", email.trim())
                                }

                                supabase.postgrest["profiles"].update(updateData) {
                                    filter { eq("id", newUserId) }
                                }
                            }

                            Toast.makeText(context, context.getString(R.string.userCreated), Toast.LENGTH_SHORT).show()
                            onDismiss()

                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(id = R.string.create), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AddUserTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF4A4A4A))
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF3F3F3),
                unfocusedContainerColor = Color(0xFFF3F3F3),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(selectedRole: String, onRoleSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf(
        "user" to stringResource(id = R.string.normalUser),
        "project_manager" to stringResource(id = R.string.manager),
        "admin" to stringResource(id = R.string.admin)
    )

    val displayRole = roles.find { it.first == selectedRole }?.second ?: "Selecione..."

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(id = R.string.userType), fontSize = 14.sp, color = Color(0xFF4A4A4A))
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            TextField(
                value = displayRole,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                roles.forEach { (roleValue, roleLabel) ->
                    DropdownMenuItem(
                        text = { Text(roleLabel, color = Color.Black) },
                        onClick = {
                            onRoleSelected(roleValue)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}