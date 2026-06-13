package com.example.tp_loomo.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tp_loomo.R
import com.example.tp_loomo.viewmodel.HistoryTaskUiModel
import com.example.tp_loomo.viewmodel.HistoryUserViewModel

@Composable
fun HistoryUserScreen(
    viewModel: HistoryUserViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    val historyList = viewModel.historyList
    val isLoading = viewModel.isLoading

    // Filtra pela pesquisa (nome da tarefa ou nome do projeto)
    val filteredList = historyList.filter {
        it.task.title.contains(searchQuery, ignoreCase = true) ||
            it.projectName.contains(searchQuery, ignoreCase = true)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1C61A2))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp) // bottom alto por causa da navbar
    ) {
        // --- CABEÇALHO CENTRALIZADO ---
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.nav_history), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = stringResource(id = R.string.viewCompletedTasks), fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- BARRA DE PESQUISA ---
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                placeholder = { Text(stringResource(id = R.string.searchTasksOrProjects), color = Color.Gray) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(id = R.string.search), tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- LISTA AGRUPADA POR MÊS ---
        if (filteredList.isEmpty()) {
            item {
                Text(
                    text = stringResource(id = R.string.noCompletedTasks),
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            val grouped = filteredList.groupBy { it.monthGroup }

            grouped.forEach { (monthLabel, tasksOfMonth) ->
                item {
                    Text(
                        text = monthLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C61A2),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(tasksOfMonth) { item ->
                    HistoryTaskCard(item)
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryTaskCard(item: HistoryTaskUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone verde de check
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFFD7F2DC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.task.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = item.projectName, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }

                Icon(Icons.Default.MoreHoriz, contentDescription = stringResource(id = R.string.more), tint = Color(0xFF1C61A2))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = item.timeSpent, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.width(20.dp))

                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = item.completionDateFormatted, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        }
    }
}
