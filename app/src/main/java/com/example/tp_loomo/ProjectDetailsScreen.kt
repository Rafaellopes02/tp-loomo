package com.example.tp_loomo

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProjectDetailsScreenPreview() {
    ProjectDetailsScreen(
        projectId = 1,
        onBackClick = {},
        projectTitle = "Trabalho Prático Redes",
        projectDesc = "Trabalho desenvolvido para a unidade curricular de Redes. Teste de visualização.",
        deadline = "16 Mai 2026",
        progress = 0.45f,
        fotosUrlDaBd = "fundo_preto",
        avatarUrls = listOf(null, null, null, null)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFB5B5B5)
@Composable
fun ProjectOptionsMenuPreview() {
    var menuExpanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .size(330.dp)
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Mais opções",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.width(158.dp).background(Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Mudar fundo", color = Color.Black, fontWeight = FontWeight.Medium) },
                    onClick = { menuExpanded = false },
                    leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Black) }
                )
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                DropdownMenuItem(
                    text = { Text("Editar", color = Color.Black, fontWeight = FontWeight.Medium) },
                    onClick = { menuExpanded = false },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Black) }
                )
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                DropdownMenuItem(
                    text = { Text("Apagar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium) },
                    onClick = { menuExpanded = false },
                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) }
                )
            }
        }
    }
}

@Composable
fun ProjectDetailsScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    projectTitle: String = "Trabalho Prático Redes",
    projectDesc: String = "Trabalho desenvolvido para a unidade curricular de Redes",
    deadline: String = "16 Mai 2026",
    progress: Float = 0.0f,
    avatarUrls: List<String?> = listOf(null, null, null, null, null),
    fotosUrlDaBd: String? = null
) {
    var selectedTab by remember { mutableStateOf("All") }
    var menuExpanded by remember { mutableStateOf(false) }

    var currentCover by remember(fotosUrlDaBd) {
        mutableStateOf<Any?>(
            when (fotosUrlDaBd) {
                "fundo_preto" -> R.drawable.fundo_preto
                "fundo_rosa" -> R.drawable.fundo_rosa
                "fundo_branco" -> R.drawable.fundo_branco
                else -> fotosUrlDaBd
            }
        )
    }
    var showCoverScreen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // --- CABEÇALHO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFFB5B5B5))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(70.dp)) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Voltar",
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Projeto",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Veja detalhes do projeto",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Mais opções",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color(0xFFF5F5F5)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mudar fundo", color = Color.Black, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    menuExpanded = false
                                    showCoverScreen = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Black)
                                }
                            )

                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                            DropdownMenuItem(
                                text = { Text("Editar", color = Color.Black, fontWeight = FontWeight.Medium) },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Black) }
                            )

                            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                            DropdownMenuItem(
                                text = { Text("Apagar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium) },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD32F2F)) }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 24.dp, end = 24.dp)
                ) {
                    OverlappingAvatars(avatarUrls = avatarUrls)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTEÚDO ---
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = projectTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = projectDesc,
                    fontSize = 15.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFEAEA))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Prazo-Final $deadline",
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color(0xFF1C61A2),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1C61A2))
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    CustomFilterChip("All", selectedTab == "All") { selectedTab = "All" }
                    CustomFilterChip("OnGoing", selectedTab == "OnGoing") { selectedTab = "OnGoing" }
                    CustomFilterChip("Concluded", selectedTab == "Concluded") { selectedTab = "Concluded" }
                }
            }
        }

        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        var isUploading by remember { mutableStateOf(false) }

        // --- SOBREPOSIÇÃO DO ECRÃ DE UPLOAD ---
        if (showCoverScreen) {
            SetCoverScreen(
                onDismiss = { showCoverScreen = false },
                onSave = { newImage ->
                    coroutineScope.launch {

                        isUploading = true
                        try {
                            // LÓGICA DE UPLOAD À PROVA DE BALA (NÃO CRASHA!)
                            val finalUrlToSave: String = when {
                                newImage == R.drawable.fundo_preto -> "fundo_preto"
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
                                {
                                    set("cover_url", finalUrlToSave)
                                }
                            ) {
                                filter { eq("id", projectId) }
                            }

                            currentCover = newImage
                            showCoverScreen = false
                            Toast.makeText(context, "Capa guardada na BD!", Toast.LENGTH_SHORT).show()

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
    }
}

@Composable
fun OverlappingAvatars(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        visibleAvatars.forEach { url ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (url != null) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                }
            }
        }

        if (remaining > 0) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$remaining",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CustomFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF1C61A2) else Color(0xFFF3F3F3))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
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
        R.drawable.fundo_rosa
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
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.Gray, modifier = Modifier.size(32.dp))
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