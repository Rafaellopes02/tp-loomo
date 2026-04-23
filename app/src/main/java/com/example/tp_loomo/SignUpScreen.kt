package com.example.tp_loomo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put // <-- IMPORT CORRIGIDO AQUI!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBackClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val loomoBlue = Color(0xFF1C61A2)
    val fieldBackgroundColor = Color(0xFFF3F3F3)
    val iconColor = Color(0xFF9E9E9E)

    // Variáveis para guardar o texto que o utilizador escreve
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    // Variável para controlar se a password está visível
    var passwordVisible by remember { mutableStateOf(false) }

    // Variáveis novas para controlar a internet e erros
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CABEÇALHO
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = stringResource(id = R.string.create_account_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 36.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // FOTO DE PERFIL
        Image(
            painter = painterResource(id = R.drawable.ic_camera_placeholder),
            contentDescription = "Adicionar foto de perfil",
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // FORMULÁRIO
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = { Text(text = stringResource(id = R.string.full_name), color = iconColor) },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Ícone Pessoa", tint = iconColor) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text(text = stringResource(id = R.string.username), color = iconColor) },
            leadingIcon = { Text("@", fontSize = 20.sp, color = iconColor, modifier = Modifier.padding(start = 12.dp, end = 4.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(text = stringResource(id = R.string.email), color = iconColor) },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Ícone Email", tint = iconColor) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = senha,
            onValueChange = { senha = it },
            placeholder = { Text(text = stringResource(id = R.string.password_placeholder), color = iconColor) },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Ícone Cadeado", tint = iconColor) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar Senha", tint = iconColor)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldBackgroundColor,
                unfocusedContainerColor = fieldBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // MENSAGEM DE ERRO
        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // BOTÃO CRIAR CONTA
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        io.github.jan.supabase.gotrue.providers.builtin.Email.let { emailProvider ->
                            supabase.auth.signUpWith(emailProvider) {
                                this.email = email
                                this.password = senha

                                // Dados Extra (Metadata)
                                this.data = buildJsonObject {
                                    put("full_name", fullName)
                                    put("username", username)
                                }
                            }
                        }

                        // SUCESSO: Navega para o próximo ecrã
                        onLoginClick()

                    } catch (e: Exception) {
                        errorMessage = "Erro ao criar conta: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = loomoBlue),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading // Desativa o botão enquanto carrega
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = stringResource(id = R.string.create_account),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RODAPÉ
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.have_account), color = Color.Black, fontSize = 16.sp)
            Text(
                text = stringResource(id = R.string.btn_login),
                color = loomoBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}