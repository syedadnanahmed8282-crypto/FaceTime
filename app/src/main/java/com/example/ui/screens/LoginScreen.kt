package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PlatinumGray
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalBlueCard
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueMedium

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    onCustomNameSignIn: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var showCustomNameInput by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RoyalBlueDark, RoyalBlueMedium)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Brand Logo Badge
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.15f))
                    .border(2.dp, ElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = "FaceTime Logo",
                    tint = ElectricCyan,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "FaceTime",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite,
                letterSpacing = 1.sp
            )

            Text(
                text = "Power by Ahmed Rasel",
                fontSize = 14.sp,
                color = PlatinumGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Primary 1-Tap Google Sign-In Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = RoyalBlueCard
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "1-Tap Google Sign-In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sign in securely with your Google Account to access contacts, chat, and HD calling.",
                        fontSize = 12.sp,
                        color = PlatinumGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = RoyalBlueDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Account",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!showCustomNameInput) {
                        TextButton(
                            onClick = { showCustomNameInput = true }
                        ) {
                            Text(
                                text = "Or test with custom name / emulator fallback",
                                fontSize = 12.sp,
                                color = PlatinumGray
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Your Name (e.g. Adnan)", color = PlatinumGray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ElectricCyan
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = PlatinumGray.copy(alpha = 0.5f),
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onCustomNameSignIn(nameInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite.copy(alpha = 0.15f),
                                contentColor = PureWhite
                            )
                        ) {
                            Text(
                                text = "Sign In with Custom Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
