package com.example.tp_loomo.ui.admin.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.tp_loomo.R


@Composable
fun CenterEmptyMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, fontSize = 15.sp)
    }
}

@Composable
fun StatsFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF1C61A2) else Color(0xFFF0F0F0))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatExportCard(title: String, subtitle: String, onDownloadClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onDownloadClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = stringResource(id = R.string.exportStatistics),
                tint = Color(0xFF1C61A2),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatsComponentsPreview() {
    var selectedTab by remember { mutableStateOf("Projetos") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            StatsFilterChip(stringResource(id = R.string.tab_projects), isSelected = selectedTab == "Projetos") { selectedTab = "Projetos" }
            StatsFilterChip(stringResource(id = R.string.tab_tasks), isSelected = selectedTab == "Tarefas") { selectedTab = "Tarefas" }
            StatsFilterChip(stringResource(id = R.string.tab_users), isSelected = selectedTab == "Utilizadores") { selectedTab = "Utilizadores" }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cards consoante o tab selecionado na Preview
        when (selectedTab) {
            "Projetos" -> {
                StatExportCard(title = "Projeto Alpha", subtitle = stringResource(id = R.string.project_card_summary, 3, 2), onDownloadClick = {})
                StatExportCard(title = "Projeto Beta", subtitle = stringResource(id = R.string.project_card_summary, 1, 1), onDownloadClick = {})
            }
            "Tarefas" -> {
                StatExportCard(title = "Tarefa 1", subtitle = stringResource(id = R.string.project_prefix, "Projeto Alpha"), onDownloadClick = {})
                StatExportCard(title = "Tarefa 2", subtitle = stringResource(id = R.string.project_prefix, "Projeto Alpha"), onDownloadClick = {})
                StatExportCard(title = "Tarefa 3", subtitle = stringResource(id = R.string.project_prefix, "Projeto Beta"), onDownloadClick = {})
            }
            "Utilizadores" -> {
                StatExportCard(title = "Rafael Lopes", subtitle = stringResource(id = R.string.user_role_prefix, stringResource(id = R.string.admin)), onDownloadClick = {})
                StatExportCard(title = "Tiago Melo", subtitle = stringResource(id = R.string.user_role_prefix, stringResource(id = R.string.project_manager_role)), onDownloadClick = {})
                StatExportCard(title = "Pablo Mendes", subtitle = stringResource(id = R.string.user_role_prefix, stringResource(id = R.string.team_member_role)), onDownloadClick = {})
                CenterEmptyMessage(stringResource(id = R.string.preview_no_more_users))
            }
        }
    }
}