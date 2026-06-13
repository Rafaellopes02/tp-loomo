package com.example.tp_loomo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tp_loomo.R

data class BottomNavItem(val title: String, val icon: ImageVector)

@Composable
fun FloatingBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    currentRole: String
) {
    val loomoBlue = Color(0xFF1C61A2)
    val lightBlue = Color(0xFFD0E0F0)
    val navHome = stringResource(id = R.string.nav_home)
    val navProjects = stringResource(id = R.string.projects)
    val navStats = stringResource(id = R.string.stats_title)
    val navUsers = stringResource(id = R.string.users)
    val navProfile = stringResource(id = R.string.porfile)
    val navTasks = stringResource(id = R.string.tasks)
    val navHistory = stringResource(id = R.string.nav_history)

    val navItems = when (currentRole) {
        "admin" -> listOf(
            BottomNavItem(navHome, Icons.Outlined.Home),
            BottomNavItem(navProjects, Icons.Outlined.Layers),
            BottomNavItem(navStats, Icons.Outlined.TrendingUp),
            BottomNavItem(navUsers, Icons.Outlined.People),
            BottomNavItem(navProfile, Icons.Outlined.Person)
        )
        "project_manager" -> listOf(
            BottomNavItem(navHome, Icons.Outlined.Home),
            BottomNavItem(navProjects, Icons.Outlined.Layers),
            BottomNavItem(navStats, Icons.Outlined.TrendingUp),
            BottomNavItem(navProfile, Icons.Outlined.Person)
        )
        else -> listOf(
            // Lista padrão (Utilizador)
            BottomNavItem(navHome, Icons.Outlined.Home),
            BottomNavItem(navTasks, Icons.Outlined.Layers),
            BottomNavItem(navHistory, Icons.Outlined.DateRange),
            BottomNavItem(navProfile, Icons.Outlined.Person)
        )
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                NavBarItem(
                    icon = item.icon,
                    label = item.title,
                    isSelected = selectedTab == index,
                    activeColor = loomoBlue,
                    activeBgColor = lightBlue,
                    onClick = { onTabSelected(index) }
                )
            }
        }
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, isSelected: Boolean, activeColor: Color, activeBgColor: Color, onClick: () -> Unit) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .background(activeBgColor, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = activeColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = label, color = activeColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    } else {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier
                .clickable { onClick() }
                .padding(8.dp)
        )
    }
}