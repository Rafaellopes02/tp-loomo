package com.example.tp_loomo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.TrendingUp
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
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardAdminScreen() {
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var nome by remember { mutableStateOf("Admin") }
    val coroutineScope = rememberCoroutineScope()
    var showAddUserModal by remember { mutableStateOf(false) }

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
                    text = "${stringResource(id = R.string.hello)}$nome",
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

        // Botões de Ação
        AdminActionCard(icon = Icons.Outlined.Add, title = stringResource(R.string.createNewProject), subtitle = stringResource(id = R.string.clickToCreateNewProject))
        Spacer(modifier = Modifier.height(12.dp))

        // BOTÃO DE ADICIONAR UTILIZADOR
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

// COMPONENTES DO MODAL DE ADICIONAR UTILIZADOR

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

            // Campos de preenchimento
            AddUserTextField(label = stringResource(id = R.string.full_name), value = fullName, onValueChange = { fullName = it })
            Spacer(modifier = Modifier.height(16.dp))

            AddUserTextField(label = stringResource(id = R.string.username), value = username, onValueChange = { username = it })
            Spacer(modifier = Modifier.height(16.dp))

            AddUserTextField(label = stringResource(id = R.string.email), value = email, onValueChange = { email = it })
            Spacer(modifier = Modifier.height(16.dp))

            RoleDropdown(selectedRole = role, onRoleSelected = { role = it })

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Criar
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