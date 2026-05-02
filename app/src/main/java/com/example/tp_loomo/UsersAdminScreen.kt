package com.example.tp_loomo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
@Serializable
data class UserProfile(
    val id: String,
    val full_name: String? = null,
    val username: String? = null,
    val avatar_url: String? = null,
    val role: String,
    val email: String? = null
)

@Composable
fun UsersAdminScreen() {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showAddUserModal by remember { mutableStateOf(false) }

    var userToEditId by remember { mutableStateOf<String?>(null) }

    fun fetchUsers() {
        coroutineScope.launch {
            isLoading = true
            try {
                val result = supabase.postgrest["profiles"]
                    .select(columns = Columns.list("id", "full_name", "username", "avatar_url", "role", "email"))
                    .decodeList<UserProfile>()
                users = result
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao carregar utilizadores: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    val filteredUsers = when (selectedFilter) {
        "Gestores" -> users.filter { it.role == "project_manager" }
        "Utilizadores" -> users.filter { it.role == "user" }
        else -> users
    }

    if (userToEditId != null) {
        EditProfileScreen(
            onBack = {
                userToEditId = null
                fetchUsers()
            },
            targetUserId = userToEditId,
            onUserDeleted = {
                userToEditId = null
                fetchUsers()
            }
        )
    } else {
        // ECRÃ NORMAL DA LISTA
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA))) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = stringResource(id = R.string.users), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Text(text = stringResource(R.string.seeAllUsers), fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChipCustom(stringResource(R.string.all), isSelected = selectedFilter == "Todos") { selectedFilter = "Todos" }
                    FilterChipCustom(stringResource(R.string.managers), isSelected = selectedFilter == "Gestores") { selectedFilter = "Gestores" }
                    FilterChipCustom(stringResource(R.string.users), isSelected = selectedFilter == "Utilizadores") { selectedFilter = "Utilizadores" }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1C61A2))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredUsers) { user ->
                            // Passa o Clique para o cartão
                            UserCard(user = user, onClick = { userToEditId = user.id })
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showAddUserModal = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 90.dp, end = 24.dp),
                containerColor = Color(0xFF1C61A2),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar", modifier = Modifier.size(32.dp))
            }

            if (showAddUserModal) {
                AddUserBottomSheet(onDismiss = {
                    showAddUserModal = false
                    fetchUsers()
                })
            }
        }
    }
}

@Composable
fun FilterChipCustom(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF1C61A2) else Color(0xFFF0F0F0),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }
}

@Composable
fun UserCard(user: UserProfile, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE0E0E0)), contentAlignment = Alignment.Center) {
                if (!user.avatar_url.isNullOrEmpty() && user.avatar_url != "null") {
                    AsyncImage(model = user.avatar_url, contentDescription = "Avatar", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.full_name ?: "Sem Nome", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = if (user.username != null) "@${user.username}" else "@...", fontSize = 14.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(6.dp))
                val (roleName, bgColor, textColor) = when(user.role) {
                    "admin" -> Triple("Admin", Color(0xFFF3E19C), Color(0xFFF57F17))
                    "project_manager" -> Triple(stringResource(R.string.manager), Color(0xFFC4AED6), Color(0xFF6A1B9A))
                    else -> Triple(stringResource(R.string.user), Color(0xFF9EBAE1), Color(0xFF1C61A2))
                }
                Box(modifier = Modifier.background(bgColor, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(text = roleName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
        }
    }
}