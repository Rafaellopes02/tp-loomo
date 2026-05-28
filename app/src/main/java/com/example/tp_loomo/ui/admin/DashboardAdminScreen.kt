package com.example.tp_loomo.ui.admin

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.model.UserProfile
import com.example.tp_loomo.viewmodel.AdminViewModel
import com.example.tp_loomo.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var showAddUserModal by remember { mutableStateOf(false) }
    var showAddProjectModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val userData = profileViewModel.userData

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
                Text(
                    text = "${stringResource(id = R.string.hello)} ${userData?.nomeCompleto?.split(" ")?.first() ?: "Admin"}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(text = "Administrador", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Estatísticas
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(stringResource(id = R.string.totalProjects), "18", Color(0xFF9EBAE1), Color(0xFF1C61A2), Modifier.weight(1f))
                AdminStatCard(stringResource(id = R.string.users), "3", Color(0xFFC4AED6), Color(0xFF6A1B9A), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCard(stringResource(id = R.string.activeTasks), "8", Color(0xFFF3E19C), Color(0xFFF57F17), Modifier.weight(1f))
                AdminStatCard(stringResource(id = R.string.completedProjects), "2", Color(0xFF90D992), Color(0xFF2E7D32), Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(stringResource(R.string.quickActions), fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

        AdminActionCard(
            icon = Icons.Outlined.TrendingUp,
            title = stringResource(id = R.string.exportStatistics),
            subtitle = stringResource(id = R.string.exportReports),
            onClick = { /* Ação futura */ }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAddUserModal) {
        AddUserBottomSheet(
            onDismiss = { showAddUserModal = false },
            viewModel = adminViewModel // Adiciona esta linha!
        )
    }

    if (showAddProjectModal) {
        AddProjectBottomSheet(
            onDismiss = { showAddProjectModal = false },
            navController = navController,
            viewModel = adminViewModel // CORRIGIDO: Argumento nomeado
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectBottomSheet(
    onDismiss: () -> Unit,
    navController: NavController,
    viewModel: AdminViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Calendar?>(null) }

    var selectedManager by remember { mutableStateOf<UserProfile?>(null) }
    val selectedUsers = remember { mutableStateListOf<UserProfile>() }
    var selectionType by remember { mutableStateOf<SelectionType?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(stringResource(id = R.string.createNewProject), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            AddUserTextField(stringResource(id = R.string.projectTitle), title) { title = it }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Project Manager",
                    fontSize = 16.sp // Garante que ambos estão nomeados
                )
                Button(onClick = { selectionType = SelectionType.MANAGER }) {
                    Text(text = "Add")
                }
            }
            selectedManager?.let { Text("Manager: ${it.full_name}", color = Color(0xFF1C61A2), fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Team Members",
                    fontSize = 16.sp
                )
                Button(onClick = { selectionType = SelectionType.TEAM }) {
                    Text(text = "Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank() || selectedManager == null) {
                        Toast.makeText(context, "Preencha o título e o manager", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    viewModel.handleCreateProject(
                        name = title,
                        desc = description,
                        managerId = selectedManager!!.id,
                        startDate = sdf.format(Date()),
                        endDate = deadline?.let { sdf.format(it.time) },
                        teamIds = selectedUsers.map { it.id },
                        onSuccess = { t, d, dead ->
                            onDismiss()
                            navController.navigate("projectDetails/${t.replace(" ","_")}/${d.replace(" ","_")}/$dead")
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) CircularProgressIndicator(color = Color.White)
                else Text(stringResource(id = R.string.create))
            }
        }
    }

    if (selectionType != null) {
        UserSelectionDialog(
            type = selectionType!!,
            viewModel = viewModel,
            onDismiss = { selectionType = null },
            onUserSelected = { user ->
                if (selectionType == SelectionType.MANAGER) selectedManager = user
                else if (!selectedUsers.contains(user)) selectedUsers.add(user)
                selectionType = null
            }
        )
    }
}

@Composable
fun UserSelectionDialog(type: SelectionType, viewModel: AdminViewModel, onDismiss: () -> Unit, onUserSelected: (UserProfile) -> Unit) {
    LaunchedEffect(Unit) {
        viewModel.loadUsersForSelection(type == SelectionType.MANAGER)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
        title = { Text("Selecionar") },
        text = {
            if (viewModel.isLoading) CircularProgressIndicator()
            else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(viewModel.usersList) { user ->
                        Text(
                            text = user.full_name ?: "Sem Nome",
                            modifier = Modifier.fillMaxWidth().clickable { onUserSelected(user) }.padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun AdminStatCard(title: String, value: String, bgColor: Color, titleColor: Color, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bgColor), modifier = modifier.height(100.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = titleColor)
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = titleColor)
        }
    }
}

@Composable
fun AdminActionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF1C61A2), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AddUserTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF3F3F3))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserBottomSheet(onDismiss: () -> Unit, viewModel: AdminViewModel) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.createNewUser), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            AddUserTextField(stringResource(id = R.string.full_name), fullName) { fullName = it }
            Spacer(modifier = Modifier.height(16.dp))

            AddUserTextField(stringResource(id = R.string.username), username) { username = it }
            Spacer(modifier = Modifier.height(16.dp))

            AddUserTextField(stringResource(id = R.string.email), email) { email = it }
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
                            Toast.makeText(context, "Utilizador criado com sucesso!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(selectedRole: String, onRoleSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("user" to "Utilizador", "project_manager" to "Gestor", "admin" to "Administrador")
    val displayRole = roles.find { it.first == selectedRole }?.second ?: "Selecione..."

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tipo de Utilizador", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            TextField(
                value = displayRole,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3)
                )
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
                roles.forEach { (roleValue, roleLabel) ->
                    DropdownMenuItem(
                        text = { Text(roleLabel) },
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

enum class SelectionType { MANAGER, TEAM }