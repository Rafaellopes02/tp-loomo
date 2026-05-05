package com.example.tp_loomo.ui.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.viewmodel.ProfileViewModel

@Composable
fun ProfileUserScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val userData = viewModel.userData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = stringResource(id = R.string.porfile), fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier.size(140.dp).clip(CircleShape).background(Color(0xFFF3F3F3)).border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (!userData?.avatarUrl.isNullOrEmpty()) {
                AsyncImage(model = userData?.avatarUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = userData?.nomeCompleto ?: "A carregar...", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = userData?.nomeUtilizador ?: "@...", fontSize = 16.sp, color = Color(0xFF1C61A2))

        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ProfileStatItem("3", stringResource(id = R.string.projects))
            ProfileStatItem("5", stringResource(id = R.string.tasks))
            ProfileStatItem("10", stringResource(id = R.string.completed))
        }

        Spacer(modifier = Modifier.height(40.dp))
        ProfileMenuButton(Icons.Outlined.PersonOutline, stringResource(id = R.string.editData), onEditProfile)
        Spacer(modifier = Modifier.height(16.dp))
        ProfileMenuButton(Icons.Outlined.Lock, stringResource(id = R.string.chagePassword), onChangePassword)

        Spacer(modifier = Modifier.height(48.dp))
        OutlinedButton(
            onClick = { viewModel.logout(onLogout) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red)
        ) {
            Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.logout), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileStatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileMenuButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null, tint = Color(0xFF1C61A2))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, color = Color(0xFF1C61A2), modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color(0xFF1C61A2))
        }
    }
}