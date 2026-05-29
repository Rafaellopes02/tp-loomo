package com.example.tp_loomo.ui.admin

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.model.UserProfile
import com.example.tp_loomo.viewmodel.AdminViewModel
import io.github.jan.supabase.gotrue.auth
import com.example.tp_loomo.data.remote.api.supabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel()
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
        AddUserBottomSheet(onDismiss = { showAddUserModal = false }, viewModel = adminViewModel)
    }

    if (showAddProjectModal) {
        AddProjectBottomSheet(
            onDismiss = { showAddProjectModal = false },
            navController = navController,
            viewModel = adminViewModel
        )
    }
}

enum class SelectionType { MANAGER, TEAM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectBottomSheet(
    onDismiss: () -> Unit,
    navController: NavController,
    viewModel: AdminViewModel
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Calendar?>(null) }

    var selectedManager by remember { mutableStateOf<UserProfile?>(null) }
    val selectedUsers = remember { mutableStateListOf<UserProfile>() }
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

            AddUserTextField(label = stringResource(id = R.string.projectTitle), value = title, onValueChange = { title = it })
            Spacer(modifier = Modifier.height(16.dp))

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

            // Prazo Limite
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Prazo-Limite", fontSize = 16.sp, color = Color.Gray)
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
                    Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gestor do Projeto
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Gestor do Projeto", fontSize = 16.sp, color = Color(0xFF4A4A4A))
                }
                Button(
                    onClick = { selectionType = SelectionType.MANAGER },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Avatar do Gestor
            if (selectedManager != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = selectedManager?.avatar_url,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color.LightGray).clickable {
                            selectedManager = null
                        },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedManager?.full_name ?: "Sem Nome", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(" - Gestor", color = Color(0xFF1C61A2), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Membros da Equipa
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Membros da Equipa", fontSize = 16.sp, color = Color(0xFF4A4A4A))
                }
                Button(
                    onClick = { selectionType = SelectionType.TEAM },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // --- LÓGICA DE AVATARES EMPILHADOS ---
            if (selectedUsers.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 36.dp),
                    horizontalArrangement = Arrangement.spacedBy((-12).dp) // O truque da sobreposição!
                ) {
                    selectedUsers.forEach { user ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape) // Borda branca para separar as imagens
                                .background(Color.LightGray)
                                .clickable { selectedUsers.remove(user) }, // Continua a dar para remover se clicares neles aqui
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.avatar_url.isNullOrBlank()) {
                                AsyncImage(
                                    model = user.avatar_url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Criar
            Button(
                onClick = {
                    if (title.isBlank() || selectedManager == null) {
                        Toast.makeText(context, "Preencha o título e selecione um Gestor", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val startDate = sdf.format(Date())
                    val endDate = deadline?.let { sdf.format(it.time) }

                    viewModel.handleCreateProject(
                        name = title.trim(),
                        desc = description.trim(),
                        managerId = selectedManager!!.id,
                        startDate = startDate,
                        endDate = endDate,
                        teamIds = selectedUsers.map { it.id },
                        onSuccess = { safeTitle, safeDesc, dateForScreen ->
                            Toast.makeText(context, "Projeto criado com sucesso!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                            navController.navigate("projectDetails/$safeTitle/$safeDesc/$dateForScreen")
                        },
                        onError = { Toast.makeText(context, "Erro: $it", Toast.LENGTH_LONG).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2))
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(id = R.string.create), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- NOVA LÓGICA DE ABRIR O DIALOG ---
    if (selectionType != null) {
        UserSelectionDialog(
            type = selectionType!!,
            viewModel = viewModel,
            // Enviamos a lista de quem já está selecionado para pintar de azul
            selectedIds = if (selectionType == SelectionType.MANAGER) listOfNotNull(selectedManager?.id) else selectedUsers.map { it.id },
            onDismiss = { selectionType = null },
            onUserSelected = { user ->
                if (selectionType == SelectionType.MANAGER) {
                    // Clica no mesmo -> Remove. Clica noutro -> Substitui
                    selectedManager = if (selectedManager?.id == user.id) null else user
                    selectionType = null // Fecha logo porque só pode haver um gestor
                } else {
                    // Lógica Team: Clica no mesmo -> Remove. Clica num novo -> Adiciona.
                    val existingUser = selectedUsers.find { it.id == user.id }
                    if (existingUser != null) {
                        selectedUsers.remove(existingUser)
                    } else {
                        selectedUsers.add(user)
                    }
                    // NOTA: Não mudamos o selectionType = null aqui! A janela não se fecha para poderes adicionar vários seguidos.
                }
            }
        )
    }
}

@Composable
fun UserSelectionDialog(
    type: SelectionType,
    viewModel: AdminViewModel,
    selectedIds: List<String>,
    onDismiss: () -> Unit,
    onUserSelected: (UserProfile) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(type) {
        viewModel.loadUsersForSelection(type == SelectionType.MANAGER)
    }

    val filteredUsers = viewModel.usersList.filter {
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

                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF1C61A2))
                } else if (filteredUsers.isEmpty()) {
                    Text("Nenhum utilizador encontrado.", fontSize = 14.sp, color = Color.Gray)
                } else {
                    LazyColumn {
                        items(filteredUsers) { user ->
                            val isSelected = selectedIds.contains(user.id)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent) // Pinta de azul claro se já foi selecionado
                                    .clickable { onUserSelected(user) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatar_url,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.full_name ?: "Sem nome", fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text("@${user.username ?: "user"}", fontSize = 12.sp, color = Color.Gray)
                                }
                                // Mostra o "certo" na lista
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selecionado", tint = Color(0xFF1C61A2))
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
fun AddUserBottomSheet(onDismiss: () -> Unit, viewModel: AdminViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }

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
                    if (email.isBlank() || fullName.isBlank() || username.isBlank()) {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.handleCreateUser(
                        email = email,
                        fullName = fullName,
                        username = username,
                        role = role,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.userCreated), Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !viewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2))
            ) {
                if (viewModel.isLoading) {
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