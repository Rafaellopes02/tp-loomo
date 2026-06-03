package com.example.tp_loomo.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tp_loomo.viewmodel.TaskDetailsViewModel

@Composable
fun TaskDetailsScreen(
    taskId: Int,
    onBackClick: () -> Unit,
    onAddRecordClick: () -> Unit = {}, // <-- Ação para o botão flutuante
    viewModel: TaskDetailsViewModel = viewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetails(taskId)
    }

    val task = viewModel.task
    val project = viewModel.project
    val isLoading = viewModel.isLoading
    val taskRecords = emptyList<String>()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            Text("Tarefa não encontrada", color = Color.Gray)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) { // Box a envolver tudo para o botão flutuante
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(Brush.linearGradient(colors = listOf(Color(0xFFDCA9F5), Color(0xFF84A6E8))))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top
                ) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Tarefa", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Veja detalhes da tarefa", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    IconButton(onClick = { /* Menu opções */ }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Mais", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(text = task.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, lineHeight = 32.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = project?.name ?: "Projeto Desconhecido", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TaskInfoCard(title = "Prazo - Limite", value = task.due_date ?: "Sem prazo", valueColor = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
                    TaskInfoCard(title = "Estado", value = if (task.status == "pending") "Em andamento" else task.status ?: "Desconhecido", valueColor = Color(0xFFD4A017), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                val progresso = task.completion_rate ?: 0
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Progresso Geral", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(text = "${progresso}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C61A2))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFFE0E0E0))) {
                    Box(modifier = Modifier.fillMaxWidth(progresso / 100f).fillMaxHeight().clip(RoundedCornerShape(7.dp)).background(Color(0xFF1C61A2)))
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (taskRecords.isNotEmpty()) {
                    Text(text = "Registos dos utilizadores", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    UserRecordCard()
                    Spacer(modifier = Modifier.height(40.dp))
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- BOTÃO CONCLUIR (Bloqueio removido para testares) ---
                Button(
                    onClick = { viewModel.completeTask(taskId) { onBackClick() } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)), shape = RoundedCornerShape(16.dp)
                ) { Text("Marcar como Concluído", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(80.dp)) // Espaço extra para o botão Flutuante não tapar
            }
        }

        // --- BOTÃO FLUTUANTE '+' (Bloqueio removido para testares) ---
        FloatingActionButton(
            onClick = onAddRecordClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 32.dp),
            containerColor = Color(0xFF1C61A2),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Registo", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun TaskInfoCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun UserRecordCard() {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF90CAF9)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column { Text(text = "Tiago Melo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black); Text(text = "8 Mai 2026", fontSize = 13.sp, color = Color.Gray) }
                }
                Box(modifier = Modifier.background(Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) { Text(text = "50%", color = Color(0xFF1C61A2), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "Braga", fontSize = 13.sp, color = Color.Gray) }
                Spacer(modifier = Modifier.width(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "3h 45min", fontSize = 13.sp, color = Color.Gray) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Criação dos ecrãs principais no Figma.", fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.size(width = 100.dp, height = 80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Gray) }
        }
    }
}