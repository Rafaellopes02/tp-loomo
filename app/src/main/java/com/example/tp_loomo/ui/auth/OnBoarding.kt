package com.example.tp_loomo.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tp_loomo.R

@Composable
fun OnboardingScreen(
    onSignUp: () -> Unit,
    onLogin: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    when (currentStep) {
        1 -> OnboardingLayout(
            imageRes = R.drawable.ic_onboarding_1,
            title = stringResource(id = R.string.onboarding1_title),
            subtitle = stringResource(id = R.string.onboarding1_subtitle),
            activeDot = 1,
            primaryButtonText = stringResource(id = R.string.btn_next),
            secondaryButtonText = stringResource(id = R.string.btn_skip),
            onNextClick = { currentStep = 2 },
            onSkipClick = onLogin
        )
        2 -> OnboardingLayout(
            imageRes = R.drawable.ic_onboarding_2,
            title = stringResource(id = R.string.onboarding2_title),
            subtitle = stringResource(id = R.string.onboarding2_subtitle),
            activeDot = 2,
            primaryButtonText = stringResource(id = R.string.btn_next),
            secondaryButtonText = stringResource(id = R.string.btn_skip),
            onNextClick = { currentStep = 3 },
            onSkipClick = onLogin
        )
        3 -> OnboardingLayout(
            imageRes = R.drawable.ic_onboarding_3,
            title = stringResource(id = R.string.onboarding3_title),
            subtitle = stringResource(id = R.string.onboarding3_subtitle),
            activeDot = 3,
            primaryButtonText = stringResource(id = R.string.btn_start_now),
            secondaryButtonText = stringResource(id = R.string.btn_already_have_account),
            onNextClick = onSignUp,
            onSkipClick = onLogin
        )
    }
}

@Composable
fun OnboardingLayout(
    imageRes: Int,
    title: String,
    subtitle: String,
    activeDot: Int,
    primaryButtonText: String,
    secondaryButtonText: String,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    // 1. Detetar a orientação do ecrã
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val LoomoBlue = Color(0xFF1C61A2)
    val TextGray = Color(0xFF8B8B8B)
    val InactiveDotColor = Color(0xFFD9D9D9)

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Ilustração Onboarding",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isActive = (index + 1) == activeDot
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isActive) 24.dp else 8.dp)
                                .background(
                                    color = if (isActive) LoomoBlue else InactiveDotColor,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoomoBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = primaryButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = secondaryButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LoomoBlue)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Ilustração Onboarding",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(top = 32.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = subtitle,
                    fontSize = 16.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isActive = (index + 1) == activeDot
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .width(if (isActive) 32.dp else 10.dp)
                            .background(
                                color = if (isActive) LoomoBlue else InactiveDotColor,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onNextClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LoomoBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = primaryButtonText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = secondaryButtonText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LoomoBlue)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(onSignUp = {}, onLogin = {})
}