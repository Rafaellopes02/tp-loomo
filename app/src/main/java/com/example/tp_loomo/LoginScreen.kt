package com.example.tp_loomo

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// IMPORTS DO SUPABASE, CORROTINAS E SERIALIZAÇÃO
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Classe de dados para ajudar o Kotlin a ler o email da tabela profiles
@Serializable
data class ProfileEmail(val email: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val loomoBlue = Color(0xFF1C61A2)
    val fieldBackgroundColor = Color(0xFFF3F3F3)
    val iconColor = Color(0xFF9E9E9E)

    // AQUI ESTÁ A MAGIA! Valores iniciais preenchidos para testes:
    var emailOrUsername by remember { mutableStateOf("rafael.lopes@ipvc.pt") }
    var senha by remember { mutableStateOf("123456789") }

    var passwordVisible by remember { mutableStateOf(false) }

    // VARIÁVEIS DE ESTADO PARA O LOGIN
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val imageSize = if (isLandscape) 100.dp else 200.dp
    val largeSpacer = if (isLandscape) 16.dp else 48.dp
    val paddingVertical = if (isLandscape) 16.dp else 32.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = paddingVertical)
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
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Login",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.login_subtitle),
                    fontSize = 16.sp,
                    color = iconColor
                )
            }
        }

        Spacer(modifier = Modifier.height(largeSpacer))

        // IMAGEM CENTRAL
        Image(
            painter = painterResource(id = R.drawable.ic_person_placeholder),
            contentDescription = "Ícone de Login",
            modifier = Modifier.size(imageSize)
        )

        Spacer(modifier = Modifier.height(largeSpacer))

        TextField(
            value = emailOrUsername,
            onValueChange = { emailOrUsername = it },
            placeholder = { Text(text = stringResource(id = R.string.username), color = iconColor) },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Ícone Utilizador", tint = iconColor) },
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.forgot_password),
            color = loomoBlue,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
                .clickable { /* Lógica futura de recuperar pass */ }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // MOSTRAR ERRO, SE EXISTIR
        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // BOTÃO DE LOGIN "DETETIVE"
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null

                    try {
                        // 1. O utilizador escreveu Email ou Username?
                        val isEmail = emailOrUsername.contains("@")

                        // 2. Descobrir qual o email verdadeiro para abrir o cofre
                        val emailToLogin = if (isEmail) {
                            emailOrUsername // Se tem '@', usamos exatamente o que ele escreveu
                        } else {
                            // Se não tem '@', vamos à tabela profiles procurar o email deste username!
                            val perfis = supabase.postgrest["profiles"]
                                .select {
                                    filter {
                                        eq("username", emailOrUsername)
                                    }
                                }.decodeList<ProfileEmail>()

                            // Se a lista vier vazia, esse username não existe
                            if (perfis.isEmpty()) {
                                throw Exception("Username não encontrado.")
                            }

                            // Se encontrou, guardamos o email associado a esse username
                            perfis.first().email
                        }

                        // 3. Fazer o Login real no "Cofre" do Supabase usando SEMPRE o email
                        io.github.jan.supabase.gotrue.providers.builtin.Email.let { emailProvider ->
                            supabase.auth.signInWith(emailProvider) {
                                this.email = emailToLogin
                                this.password = senha
                            }
                        }

                        // SUCESSO: O utilizador entra na aplicação!
                        onLoginClick()

                    } catch (e: Exception) {
                        // Mostra a mensagem de erro correta
                        if (e.message == "Username não encontrado.") {
                            errorMessage = e.message
                        } else {
                            errorMessage = "Credenciais inválidas. Tenta novamente."
                        }
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
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = stringResource(id = R.string.btn_login), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(largeSpacer))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.no_account), color = Color.Black, fontSize = 16.sp)
            Text(
                text = stringResource(id = R.string.register_now),
                color = loomoBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}