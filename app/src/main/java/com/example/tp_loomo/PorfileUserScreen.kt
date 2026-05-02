package com.example.tp_loomo

import android.R.attr.fontWeight
import android.R.attr.text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileUserScreen(onLogout: () -> Unit, onEditProfile: () -> Unit, onChangePassword: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var nomeCompleto by remember { mutableStateOf("A carregar...") }
    var nomeUtilizador by remember { mutableStateOf("@...") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    // Carregar dados reais do Supabase
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                nomeCompleto = user.userMetadata?.get("full_name").toString().replace("\"", "")
                nomeUtilizador = "@" + user.userMetadata?.get("username").toString().replace("\"", "")
                avatarUrl = user.userMetadata?.get("avatar_url")?.toString()?.replace("\"", "")
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
        Spacer(modifier = Modifier.height(32.dp))

        // TÍTULO DO ECRÃ
        Text(
            text = stringResource(id = R.string.porfile),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = stringResource(id = R.string.viewPorfile),
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // FOTO DE PERFIL
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F3F3))
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Verifica se o utilizador tem foto guardada
            if (!avatarUrl.isNullOrEmpty() && avatarUrl != "null") {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = "Foto de Perfil Padrão",
                    modifier = Modifier.size(80.dp),
                    tint = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INFO NOME
        Text(text = nomeCompleto, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = nomeUtilizador, fontSize = 16.sp, color = Color(0xFF1C61A2), fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        // ESTATÍSTICAS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatItem(number = "3", label = stringResource(id = R.string.projects))
            ProfileStatItem(number = "5", label = stringResource(id = R.string.tasks))
            ProfileStatItem(number = "10", label = stringResource(id = R.string.completed))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // BOTÕES DE EDIÇÃO
        ProfileMenuButton(
            icon = Icons.Outlined.PersonOutline,
            label = stringResource(id = R.string.editData),
            onClick = onEditProfile
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProfileMenuButton(
            icon = Icons.Outlined.Lock,
            label = stringResource(id = R.string.chagePassword),
            onClick = onChangePassword
        )

        Spacer(modifier = Modifier.height(48.dp))

        // BOTÃO SAIR
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    supabase.auth.signOut()
                    onLogout()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = borderStroke(1.dp, Color.Red),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.logout), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ProfileStatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileMenuButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1C61A2))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, color = Color(0xFF1C61A2), fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF1C61A2))
        }
    }
}

fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)