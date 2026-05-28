package com.habittracker.app.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.habittracker.app.ui.theme.Indigo600
import com.habittracker.app.ui.theme.Purple700
import com.habittracker.app.ui.theme.Violet600

@Composable
fun LoginScreen(
    onLoginSuccess: (setupDone: Boolean) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.handleGoogleSignInResult(task)
        } else {
            viewModel.resetState()
        }
    }

    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess((state as LoginState.Success).setupDone)
        }
    }

    val gradient = Brush.linearGradient(listOf(Indigo600, Violet600, Purple700))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(gradient, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 36.sp)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Habit Tracker",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Build powerful daily routines.\nTrack, improve, stay consistent.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Feature grid 2x2
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureChip("📅", "Daily check-ins", Modifier.weight(1f))
                    FeatureChip("🔔", "Smart reminders", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureChip("📊", "Streak tracking", Modifier.weight(1f))
                    FeatureChip("☁️", "Syncs everywhere", Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                // Google Sign-In button
                when (val s = state) {
                    is LoginState.Loading -> CircularProgressIndicator(color = Indigo600)
                    else -> {
                        Button(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(com.habittracker.app.R.string.default_web_client_id))
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(context, gso)
                                launcher.launch(client.signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1E293B)
                            ),
                            elevation = ButtonDefaults.buttonElevation(2.dp)
                        ) {
                            Text("🔵  Continue with Google", fontWeight = FontWeight.SemiBold)
                        }

                        if (s is LoginState.Error) {
                            Spacer(Modifier.height(8.dp))
                            Text(s.message, color = Color(0xFFEF4444), fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Your data is stored securely and never shared.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureChip(emoji: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
        }
    }
}
