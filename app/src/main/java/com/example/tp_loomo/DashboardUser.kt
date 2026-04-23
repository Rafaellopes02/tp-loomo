package com.example.tp_loomo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// IMPORTS NECESSÁRIOS PARA O SUPABASE
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardUserScreen() {
    val backgroundColor = Color(0xFFFAFAFA)

    // 1. Variável de Estado para guardar o Nome do Utilizador
    var userName by remember { mutableStateOf("A carregar...") }
    val coroutineScope = rememberCoroutineScope()

    // 2. Assim que o ecrã abre, vai procurar quem está com o login feito!
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                // Pede ao Supabase o utilizador atual
                val currentUser = supabase.auth.currentUserOrNull()

                // Se existir utilizador, lê o "full_name" guardado no registo
                if (currentUser != null) {
                    val fullName = currentUser.userMetadata?.get("full_name").toString()

                    // Remove as aspas chatas que vêm do formato JSON
                    userName = fullName.replace("\"", "")
                } else {
                    userName = "Visitante" // Caso o login tenha expirado
                }
            } catch (e: Exception) {
                userName = "Visitante"
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            FloatingBottomNavBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 3. CABEÇALHO COM O NOME VERDADEIRO DA BASE DE DADOS
            DashboardHeader(nome = userName)

            Spacer(modifier = Modifier.height(32.dp))

            // 4. CARTÃO DE PROJETO EM DESTAQUE
            ProjectHighlightCard()

            Spacer(modifier = Modifier.height(32.dp))

            // 5. SECÇÃO TAREFAS DE HOJE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tarefas de Hoje",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Ver todas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C61A2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LISTA DE TAREFAS
            TaskCard(title = "Desenvolver Protótipo Figma", project = "Trabalho Prático Redes")
            Spacer(modifier = Modifier.height(12.dp))
            TaskCard(title = "Criar plano no Trello", project = "Trabalho Prático Redes")

            // Espaço extra no fundo para não ficar colado à barra de navegação
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DashboardHeader(nome: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Imagem de Perfil (Placeholder circular)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Color.Gray)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Olá, $nome",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = "Bem-vindo de volta!",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ProjectHighlightCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Imagem de Topo do Cartão (Usando um gradiente como placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF), Color(0xFF1C61A2))
                        )
                    )
            )

            // Conteúdo do Cartão
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trabalho Prático Redes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Mais opções", tint = Color(0xFF1C61A2))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Desenvolver Protótipo Figma - ",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // Tag de Data
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Até 16 Mai 2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(title: String, project: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.FormatListBulleted,
                contentDescription = "Tarefa",
                tint = Color(0xFF1C61A2),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = project,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FloatingBottomNavBar() {
    val loomoBlue = Color(0xFF1C61A2)
    val lightBlue = Color(0xFFD0E0F0)

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
            // Item Ativo (Home)
            Box(
                modifier = Modifier
                    .background(lightBlue, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Home, contentDescription = "Home", tint = loomoBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Home", color = loomoBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Itens Inativos
            Icon(Icons.Outlined.Layers, contentDescription = "Projetos", tint = Color.Gray)
            Icon(Icons.Outlined.DateRange, contentDescription = "Calendário", tint = Color.Gray)
            Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardUserScreenPreview() {
    DashboardUserScreen()
}