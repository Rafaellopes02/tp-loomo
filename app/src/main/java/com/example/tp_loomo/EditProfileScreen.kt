package com.example.tp_loomo

import android.R.attr.data
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
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

    // LAUNCHER DO SELETOR DE FOTOS DO ANDROID
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploadingPhoto = true
                try {
                    val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                    // 1. Converter a imagem em bytes
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        // 2. Criar um nome único para a foto
                        val filename = "${userId}_${UUID.randomUUID()}.jpg"

                        supabase.storage["avatars"].upload(filename, bytes, upsert = true)

                        // 4. Obter o URL público da foto
                        val publicUrl = supabase.storage["avatars"].publicUrl(filename)
                        avatarUrl = publicUrl
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao carregar foto: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            fullName = user.userMetadata?.get("full_name").toString().replace("\"", "")
            username = user.userMetadata?.get("username").toString().replace("\"", "")
            avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
            email = user.email ?: ""
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
        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", modifier = Modifier.size(32.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(id = R.string.porfile), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(id = R.string.editeDataHere), fontSize = 16.sp, color = Color.Gray)
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
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(40.dp), tint = loomoBlue)
                }
            }
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color.White, shadowElevation = 4.dp) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.padding(8.dp).size(20.dp), tint = loomoBlue)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(stringResource(id = R.string.edit), modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End, color = loomoBlue, fontWeight = FontWeight.Bold)

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
                        val nomeLimpo = fullName.trim()
                        val userLimpo = username.trim()

                        val userId = supabase.auth.currentUserOrNull()?.id

                        // 1. Atualizar Auth
                        supabase.auth.modifyUser {
                            data = buildJsonObject {
                                put("full_name", nomeLimpo)
                                put("username", userLimpo)
                                if (avatarUrl != null) put("avatar_url", avatarUrl)
                            }
                        }

                        // 2. Atualizar na Tabela Profiles
                        if (userId != null) {
                            val updateData = buildJsonObject {
                                put("full_name", nomeLimpo)
                                put("username", userLimpo)
                            }

                            supabase.postgrest["profiles"].update(updateData) {
                                filter { eq("id", userId) }
                            }
                        }

                        Toast.makeText(context, context.getString(R.string.porfileUpdated), Toast.LENGTH_SHORT).show()
                        onBack()

                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoading || isUploadingPhoto) Color.LightGray else if (isPressed) Color(0xFF8FB1D0) else Color(0xFF1C61A2)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(id = R.string.Confirm), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun EditField(value: String, onValueChange: (String) -> Unit, fieldBg: Color, enabled: Boolean = true) {
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