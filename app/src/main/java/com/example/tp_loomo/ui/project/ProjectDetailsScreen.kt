package com.example.tp_loomo.ui.project

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.data.remote.api.supabase // Certifica-te de importar a tua variável supabase corretamente
import com.example.tp_loomo.viewmodel.ProjectDetailsViewModel
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

data class MockTask(val title: String, val time: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    projectId: Int,
    onBackClick: () -> Unit,
    viewModel: ProjectDetailsViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf("Todas") }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- ESTADOS DA CAPA (TUA LÓGICA) ---
    var showCoverScreen by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    // --- ESTADOS DO MODO DE EDIÇÃO ---
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }

    LaunchedEffect(projectId) {
        viewModel.loadProjectDetails(projectId)
    }

    val project = viewModel.project
    val teamMembers = viewModel.teamMembers
    val isLoading = viewModel.isLoading

    // Define a capa atual com base nos dados que o ViewModel carregou do projeto
    var currentCover by remember(project?.cover_url) {
        mutableStateOf<Any?>(
            when (project?.cover_url) {
                "fundo_preto" -> R.drawable.fundo_preto
                "fundo_rosa" -> R.drawable.fundo_rosa
                "fundo_azul" -> R.drawable.fundo_azul
                "fundo_branco" -> R.drawable.fundo_branco
                else -> project?.cover_url
            }
        )
    }

    val mockTasks = listOf(
        MockTask("Desenvolver Protótipo Figma", "Hoje - 17.00H"),
        MockTask("Cumprir Requisitos Funcionais", "Amanhã - 19.30H"),
        MockTask("Modelo de Dados", "17 Mai 2026 - 10.00H")
    )

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    if (project == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            Text("Projeto não encontrado", color = Color.Gray)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // --- CABEÇALHO ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(brush = Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8))))
                ) {

                    // A TUA LÓGICA DE IMAGEM DE FUNDO
                    if (currentCover != null) {
                        AsyncImage(
                            model = currentCover,
                            contentDescription = "Capa do Projeto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // A TUA CAMADA ESCURA PARA DESTACAR O TEXTO
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.29f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(40.dp))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Projeto", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "Veja detalhes do projeto", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "Mais", tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mudar fundo", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        showCoverScreen = true // ABRE O TEU ECRÃ DE CAPA
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Editar", color = Color.Black, fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Color.Black) },
                                    onClick = {
                                        showMenu = false
                                        editName = project.name
                                        editDescription = project.description ?: ""
                                        isEditing = true
                                    }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

                                DropdownMenuItem(
                                    text = { Text("Concluído", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    onClick = { showMenu = false }
                                )

                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = Color.LightGray)

                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = Color(0xFFD32F2F)) },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 24.dp, end = 24.dp)) {
                        OverlappingAvatars(avatarUrls = teamMembers.map { it.avatar_url })
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nome do Projeto") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
                            label = { Text("Descrição") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isEditing = false }) {
                                Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateProject(projectId, editName, editDescription) {
                                        isEditing = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(text = project.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = project.description ?: "Sem descrição.", fontSize = 15.sp, color = Color.Gray, lineHeight = 22.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFEBEE)).padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Prazo-Final: ${project.end_date ?: "Sem prazo"}",
                                color = Color(0xFFD32F2F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(text = "50%", color = Color(0xFF1C61A2), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFFE0E0E0))) {
                        Box(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(Color(0xFF1C61A2)))
                    }
                }
            }

            // --- TABS (FILTROS) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomFilterChip("Todas", selectedTab == "Todas") { selectedTab = "Todas" }
                    CustomFilterChip("Andamento", selectedTab == "Andamento") { selectedTab = "Andamento" }
                    CustomFilterChip("Concluído", selectedTab == "Concluído") { selectedTab = "Concluído" }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- LISTA DE TAREFAS ---
            items(mockTasks) { task ->
                TaskItemCard(title = task.title, time = task.time)
            }
        }

        // --- A TUA LÓGICA DE SOBREPOSIÇÃO DO ECRÃ DE UPLOAD ---
        if (showCoverScreen) {
            SetCoverScreen(
                onDismiss = { showCoverScreen = false },
                onSave = { newImage ->
                    coroutineScope.launch {
                        isUploading = true
                        try {
                            val finalUrlToSave: String = when {
                                newImage == R.drawable.fundo_preto -> "fundo_preto"
                                newImage == R.drawable.fundo_branco -> "fundo_branco"
                                newImage == R.drawable.fundo_azul -> "fundo_azul"
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

                            // Força a ViewModel a recarregar o projeto para manter tudo sincronizado
                            viewModel.loadProjectDetails(projectId)

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

        // --- MODAL DE CONFIRMAÇÃO DE ELIMINAÇÃO ---
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = "Eliminar Projeto", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black) },
                text = { Text(text = "Pretende mesmo eliminar este projeto? Esta ação é permanente e não poderá ser revertida.", fontSize = 15.sp, color = Color.DarkGray) },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Sim", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TaskItemCard(title: String, time: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FormatListBulleted,
                contentDescription = null,
                tint = Color(0xFF1C61A2),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = time, fontSize = 13.sp, color = Color.Gray)
            }
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
                modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color(0xFFFFB74D)),
                contentAlignment = Alignment.Center
            ) {
                if (!url.isNullOrEmpty() && url != "null") {
                    AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                }
            }
        }
        if (remaining > 0) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("+$remaining", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CustomFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) Color(0xFF1C61A2) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// O TEU COMPONENTE DE ESCOLHA DE CAPA INTACTO
@Composable
fun SetCoverScreen(
    onDismiss: () -> Unit,
    onSave: (Any) -> Unit
) {
    var selectedImage by remember { mutableStateOf<Any?>(null) }

    val systemBackgrounds = listOf(
        R.drawable.fundo_preto,
        R.drawable.fundo_rosa,
        R.drawable.fundo_branco,
        R.drawable.fundo_azul
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
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.Gray, modifier = Modifier.size(32.dp))
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