package com.example.tp_loomo.ui.project

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.data.remote.api.supabase
import com.example.tp_loomo.viewmodel.TaskDetailsViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

// --- MODELO PARA A BASE DE DADOS ---
@Serializable
data class TaskRecordInsert(
    val task_id: Int,
    val user_id: String,
    val progress: Int,
    val location: String,
    val date: String,
    val time_spent: String,
    val observations: String,
    val photo_url: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRecordFormScreen(
    taskId: Int,
    onBackClick: () -> Unit,
    viewModel: TaskDetailsViewModel = viewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetails(taskId)
    }

    val task = viewModel.task
    val project = viewModel.project
    val isLoading = viewModel.isLoading

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableFloatStateOf(0.2f) }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }

    // Estados para a foto e para o botão de guardar
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Lançador da galeria
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImage = uri.toString()
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
    ) {
        // --- CABEÇALHO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8))))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Registo", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "Adicionar informação à tarefa", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // --- FORMULÁRIO ---
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

            Text(text = task?.title ?: "A carregar...", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, lineHeight = 32.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project?.name ?: "Projeto Desconhecido", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Progresso", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(text = "${(progress * 100).roundToInt()}%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C61A2))
            }
            Slider(
                value = progress, onValueChange = { progress = it },
                colors = SliderDefaults.colors(thumbColor = Color(0xFF1C61A2), activeTrackColor = Color(0xFF1C61A2), inactiveTrackColor = Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Localização", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = location, onValueChange = { location = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = date, onValueChange = { date = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Tempo Dispensado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeSpent, onValueChange = { timeSpent = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Observações", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = observations, onValueChange = { observations = it }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Anexar Ficheiros", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (selectedImage != null) 200.dp else 80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.LightGray, style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                        )
                    }
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImage != null) {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clica para anexar uma foto", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button

                    coroutineScope.launch {
                        isSaving = true
                        try {
                            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Utilizador não autenticado")
                            var finalUrl: String? = null

                            // 1. Faz upload da foto se existir
                            if (selectedImage != null) {
                                val uri = android.net.Uri.parse(selectedImage)
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val byteArray = inputStream?.readBytes()

                                if (byteArray != null) {
                                    val fileName = "task_${taskId}_${System.currentTimeMillis()}.jpg"
                                    val bucket = supabase.storage["task_photos"]
                                    bucket.upload(fileName, byteArray)
                                    finalUrl = bucket.publicUrl(fileName)
                                }
                            }

                            // 2. Prepara os dados para a tabela
                            val progressoFinal = (progress * 100).roundToInt()
                            val novoRegisto = TaskRecordInsert(
                                task_id = taskId,
                                user_id = userId,
                                progress = progressoFinal,
                                location = location,
                                date = date,
                                time_spent = timeSpent,
                                observations = observations,
                                photo_url = finalUrl
                            )

                            // 3. Insere na tabela task_records
                            supabase.postgrest["task_records"].insert(novoRegisto)

                            supabase.postgrest["tasks"].update(
                                mapOf("completion_rate" to progressoFinal)
                            ) {
                                filter { eq("id", taskId) }
                            }

                            Toast.makeText(context, "Registo guardado com sucesso!", Toast.LENGTH_SHORT).show()
                            onBackClick()

                        } catch (e: Exception) {
                            android.util.Log.e("TaskRecord", "Erro a guardar: ${e.message}")
                            Toast.makeText(context, "Erro a guardar: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Concluído", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}