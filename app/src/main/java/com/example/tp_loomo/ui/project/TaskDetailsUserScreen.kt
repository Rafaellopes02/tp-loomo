package com.example.tp_loomo.ui.project

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tp_loomo.R
import com.example.tp_loomo.viewmodel.TaskDetailsViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsUserScreen(
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

    var progress by remember { mutableFloatStateOf(0.2f) }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }

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
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.task_details_title), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = stringResource(id = R.string.task_details_subtitle), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // --- CONTEÚDO / FORMULÁRIO ---
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

            Text(text = task?.title ?: stringResource(id = R.string.state_loading), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, lineHeight = 32.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project?.name ?: stringResource(id = R.string.task_project_unknown), fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(32.dp))

            // 1. Progresso
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(id = R.string.pdf_th_progress), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(text = "${(progress * 100).roundToInt()}%", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C61A2))
            }
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF1C61A2),
                    activeTrackColor = Color(0xFF1C61A2),
                    inactiveTrackColor = Color(0xFFE0E0E0)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Localização
            Text(text = stringResource(id = R.string.pdf_th_location), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Data e Tempo Dispensado
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(id = R.string.pdf_th_date), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = date, onValueChange = { date = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(id = R.string.pdf_th_time), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = timeSpent, onValueChange = { timeSpent = it },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Observações
            Text(text = stringResource(id = R.string.pdf_th_observations), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = observations, onValueChange = { observations = it },
                modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Anexar Ficheiros
            Text(text = stringResource(id = R.string.form_attach_files), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.LightGray,
                            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                        )
                    }
                    .clickable { /* Ação de anexar foto */ },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.form_attach_placeholder), color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 6. Botão Concluído
            Button(
                onClick = {
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = stringResource(id = R.string.Confirm), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}