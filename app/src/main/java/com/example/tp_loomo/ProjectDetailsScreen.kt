package com.example.tp_loomo

import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProjectDetailsScreenPreview() {
    // Aqui chamas a tua página com dados falsos (mock data) só para veres como fica
    ProjectDetailsScreen(
        onBackClick = {},
        projectTitle = "Trabalho Prático Redes",
        projectDesc = "Trabalho desenvolvido para a unidade curricular de Redes. Teste de visualização.",
        deadline = "16 Mai 2026",
        progress = 0.45f,
        avatarUrls = listOf(null, null, null, null) // Simulamos 4 pessoas (sem foto, vai mostrar o ícone padrão)
    )
}

@Composable
fun ProjectDetailsScreen(
    onBackClick: () -> Unit,
    projectTitle: String = "Trabalho Prático Redes",
    projectDesc: String = "Trabalho desenvolvido para a unidade curricular de Redes",
    deadline: String = "16 Mai 2026",
    progress: Float = 0.0f, // 0.0f a 1.0f
    avatarUrls: List<String?> = listOf(null, null, null, null, null) // Passa os avatars verdadeiros aqui
) {
    // Estado para sabermos que filtro está selecionado
    var selectedTab by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- CABEÇALHO (HEADER) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0xFFB5B5B5)) // Ajusta para a cor cinzenta da tua imagem
        ) {
            // Barra de Navegação (Topo)
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

                IconButton(onClick = { /* Abrir menu de opções */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Mais opções",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Membros da Equipa (Canto Inferior Direito do Cabeçalho)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 24.dp)
            ) {
                OverlappingAvatars(avatarUrls = avatarUrls)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CONTEÚDO DO PROJETO ---
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
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECÇÃO DO PRAZO E PROGRESSO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill do Prazo Final
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEAEA)) // Fundo vermelho claro
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Prazo-Final $deadline",
                        color = Color(0xFFD32F2F), // Texto vermelho escuro
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Percentagem
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = Color(0xFF1C61A2), // Azul
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Barra de Progresso Customizada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFEEEEEE)) // Fundo cinza da barra
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress) // Preenche consoante o progresso
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1C61A2)) // Cor azul do preenchimento
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- FILTROS (TABS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                CustomFilterChip(
                    text = "All",
                    isSelected = selectedTab == "All",
                    onClick = { selectedTab = "All" }
                )
                CustomFilterChip(
                    text = "OnGoing",
                    isSelected = selectedTab == "OnGoing",
                    onClick = { selectedTab = "OnGoing" }
                )
                CustomFilterChip(
                    text = "Concluded",
                    isSelected = selectedTab == "Concluded",
                    onClick = { selectedTab = "Concluded" }
                )
            }
        }
    }
}

// COMPONENTE: Avatares Sobrepostos com borda branca
@Composable
fun OverlappingAvatars(avatarUrls: List<String?>, maxAvatars: Int = 3) {
    // Filtramos os N primeiros para mostrar
    val visibleAvatars = avatarUrls.take(maxAvatars)
    val remaining = avatarUrls.size - maxAvatars

    // O Arrangement com valor negativo faz o overlap perfeito
    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
        visibleAvatars.forEach { url ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape) // A borda branca que separa as caras
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

        // Se houver mais pessoas que o maxAvatars, mostra o "+X"
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

// COMPONENTE: Botão de Filtro Customizado
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