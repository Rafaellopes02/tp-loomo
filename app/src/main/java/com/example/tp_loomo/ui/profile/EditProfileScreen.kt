package com.example.tp_loomo.ui.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.data.remote.model.UserProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    targetUserId: String? = null,
    onUserDeleted: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val loomoBlue = Color(0xFF1C61A2)
    val fieldBg = Color(0xFFF3F3F3)
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currentUserId = targetUserId ?: supabase.auth.currentUserOrNull()?.id

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingPhoto = true
                try {
                    val userId = currentUserId ?: return@launch

                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val filename = "${userId}_${UUID.randomUUID()}.jpg"
                        supabase.storage["avatars"].upload(filename, bytes, upsert = true)
                        val publicUrl = supabase.storage["avatars"].publicUrl(filename)
                        avatarUrl = publicUrl
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Erro ao carregar foto: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (targetUserId != null) {
            coroutineScope.launch {
                try {
                    val result = supabase.postgrest["profiles"]
                        .select(
                            columns = Columns.list(
                                "id",
                                "full_name",
                                "username",
                                "avatar_url",
                                "role",
                                "email"
                            )
                        ) {
                            filter { eq("id", targetUserId) }
                        }
                        .decodeSingle<UserProfile>()

                    fullName = result.full_name ?: ""
                    username = result.username ?: ""
                    avatarUrl = result.avatar_url
                    email = result.email ?: "Sem e-mail registado"

                } catch (_: Exception) { // Aviso amarelo removido (substituindo e por _)
                    Toast.makeText(
                        context,
                        "Erro ao carregar dados do utilizador",
                        Toast.LENGTH_SHORT
                    ).show()
                    onBack()
                }
            }
        } else {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            try {
                val result = supabase.postgrest["profiles"]
                    .select(
                        columns = Columns.list(
                            "id",
                            "full_name",
                            "username",
                            "avatar_url",
                            "email"
                        )
                    ) {
                        filter { eq("id", user.id) }
                    }
                    .decodeSingle<UserProfile>()

                fullName = result.full_name ?: ""
                username = result.username ?: ""
                avatarUrl = result.avatar_url
                email = result.email ?: user.email ?: ""
            } catch (e: Exception) {
                // fallback para metadata se falhar
                fullName = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "") ?: ""
                username = user.userMetadata?.get("username")?.toString()?.replace("\"", "") ?: ""
                email = user.email ?: ""
            }
        }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CABEÇALHO
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(32.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(id = R.string.porfile),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(id = R.string.editeDataHere),
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // FOTO DE PERFIL
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable(enabled = !isUploadingPhoto) {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F3F3)),
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingPhoto) {
                    CircularProgressIndicator(color = loomoBlue)
                } else if (!avatarUrl.isNullOrEmpty() && avatarUrl != "null") {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = loomoBlue
                    )
                }
            }
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = loomoBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            stringResource(id = R.string.edit),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = loomoBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // CAMPOS
        EditField(value = fullName, onValueChange = { fullName = it }, fieldBg = fieldBg)
        Spacer(modifier = Modifier.height(16.dp))
        EditField(value = username, onValueChange = { username = it }, fieldBg = fieldBg)
        Spacer(modifier = Modifier.height(16.dp))
        EditField(value = email, onValueChange = {}, fieldBg = fieldBg, enabled = false)

        Spacer(modifier = Modifier.height(60.dp))

        // BOTÃO CONFIRMAR
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val idToUpdate = targetUserId ?: currentUserId
                        if (idToUpdate != null) {
                            // 1. Guarda sempre localmente primeiro
                            val repository = com.example.tp_loomo.data.repository.ProfileRepository(context)
                            repository.savePendingUpdate(
                                fullName = fullName.trim(),
                                username = username.trim(),
                                avatarUrl = avatarUrl
                            )

                            // 2. Tenta sincronizar imediatamente com a API
                            val syncedNow = repository.syncPendingUpdate()

                            if (syncedNow) {
                                Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            } else {
                                // 3. Se falhou (sem rede), agenda sync para quando houver rede
                                agendarSync(context)
                                Toast.makeText(
                                    context,
                                    "Sem rede. As alterações serão sincronizadas automaticamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            onBack()
                        }
                    } catch (e: Exception) {
                        // Fallback de segurança: guarda offline e agenda sync
                        val repository = com.example.tp_loomo.data.repository.ProfileRepository(context)
                        repository.savePendingUpdate(fullName.trim(), username.trim(), avatarUrl)
                        agendarSync(context)
                        Toast.makeText(
                            context,
                            "Erro de rede. As alterações foram guardadas localmente.",
                            Toast.LENGTH_LONG
                        ).show()
                        onBack()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoading || isUploadingPhoto) Color.LightGray else if (isPressed) Color(
                    0xFF8FB1D0
                ) else Color(0xFF1C61A2)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    stringResource(id = R.string.Confirm),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        // BOTÃO REMOVER
        if (targetUserId != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Vermelho
            ) {
                Text(
                    "Remover Utilizador",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // ALERTA DE CONFIRMAÇÃO PARA APAGAR
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Utilizador", fontWeight = FontWeight.Bold) },
            text = { Text("Pretende mesmo eliminar este utilizador? Esta ação não pode ser desfeita e removerá todos os dados a ele associados.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                if (targetUserId != null) {
                                    supabase.postgrest["profiles"].delete {
                                        filter {
                                            eq(
                                                "id",
                                                targetUserId
                                            )
                                        }
                                    }
                                    Toast.makeText(
                                        context,
                                        "Utilizador removido com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showDeleteDialog = false
                                    onUserDeleted()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Erro ao remover: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Sim, eliminar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    fieldBg: Color,
    enabled: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            disabledContainerColor = fieldBg.copy(alpha = 0.5f),
            disabledTextColor = Color.Gray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

private fun agendarSync(context: Context) {
    val request = androidx.work.OneTimeWorkRequestBuilder<com.example.tp_loomo.worker.SyncWorker>()
        .setConstraints(
            androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()
        )
        .build()
    androidx.work.WorkManager.getInstance(context)
        .enqueueUniqueWork("profile_sync", androidx.work.ExistingWorkPolicy.REPLACE, request)
}