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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tp_loomo.R
import com.example.tp_loomo.utils.avatarDbValueToResource
import com.example.tp_loomo.viewmodel.TaskDetailsViewModel
import com.example.tp_loomo.viewmodel.TaskRecordUiModel

@Composable
fun TaskDetailsScreen(
    taskId: Int,
    onBackClick: () -> Unit,
    onAddRecordClick: () -> Unit = {},
    viewModel: TaskDetailsViewModel = viewModel()
) {
    LaunchedEffect(taskId) {
        viewModel.loadTaskDetails(taskId)
    }

    val task = viewModel.task
    val project = viewModel.project
    val isLoading = viewModel.isLoading
    val taskRecords = viewModel.taskRecords

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            Text(text = stringResource(id = R.string.task_not_found), color = Color.Gray)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(id = R.string.task_details_title), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Text(text = stringResource(id = R.string.task_details_subtitle), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    IconButton(onClick = { /* Menu opções */ }) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = stringResource(id = R.string.more_options_content_desc), tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(text = task.title, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black, lineHeight = 32.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = project?.name ?: stringResource(id = R.string.task_project_unknown), fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(28.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TaskInfoCard(title = stringResource(id = R.string.projectDeadline), value = task.due_date ?: stringResource(id = R.string.no_deadline_short), valueColor = Color(0xFFD32F2F), modifier = Modifier.weight(1f))

                    val statusText = if (task.status == "pending") stringResource(id = R.string.filter_in_progress) else task.status ?: stringResource(id = R.string.state_unknown)
                    TaskInfoCard(title = stringResource(id = R.string.pdf_th_status), value = statusText, valueColor = Color(0xFFD4A017), modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                val progresso = task.completion_rate ?: 0
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.task_general_progress), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(text = "${progresso}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1C61A2))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp)).background(Color(0xFFE0E0E0))) {
                    Box(modifier = Modifier.fillMaxWidth(progresso / 100f).fillMaxHeight().clip(RoundedCornerShape(7.dp)).background(Color(0xFF1C61A2)))
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (taskRecords.isNotEmpty()) {
                    Text(text = stringResource(id = R.string.task_user_records_title), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Spacer(modifier = Modifier.height(16.dp))

                    taskRecords.forEach { recordModel ->
                        UserRecordCard(uiModel = recordModel)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (viewModel.isAssignedToCurrentUser) {
                    Button(
                        onClick = { viewModel.completeTask(taskId) { onBackClick() } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C61A2)), shape = RoundedCornerShape(16.dp)
                    ) { Text(text = stringResource(id = R.string.task_btn_complete), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (viewModel.isAssignedToCurrentUser) {
            FloatingActionButton(
                onClick = onAddRecordClick,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 32.dp),
                containerColor = Color(0xFF1C61A2),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
            }
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
fun UserRecordCard(uiModel: TaskRecordUiModel) {
    val record = uiModel.record
    val profile = uiModel.userProfile

    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFB74D)), contentAlignment = Alignment.Center) {
                        if (!profile?.avatar_url.isNullOrEmpty()) {
                            AsyncImage(model = avatarDbValueToResource(profile?.avatar_url), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = profile?.full_name ?: stringResource(id = R.string.unnamed_user), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = record.date.ifEmpty { stringResource(id = R.string.record_no_date) }, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                Box(modifier = Modifier.background(Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(text = "${record.progress}%", color = Color(0xFF1C61A2), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = record.location.ifEmpty { stringResource(id = R.string.record_no_location) }, fontSize = 13.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = record.time_spent.ifEmpty { "N/A" }, fontSize = 13.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = record.observations.ifEmpty { stringResource(id = R.string.record_no_observations) }, fontSize = 14.sp, color = Color.DarkGray)

            if (!record.photo_url.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.size(width = 100.dp, height = 80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEEEEEE)), contentAlignment = Alignment.Center) {
                    AsyncImage(model = record.photo_url, contentDescription = stringResource(id = R.string.record_photo_content_desc), modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
        }
    }
}