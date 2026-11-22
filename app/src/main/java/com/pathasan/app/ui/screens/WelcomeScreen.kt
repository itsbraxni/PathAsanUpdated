package com.pathasan.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pathasan.app.R
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.ui.components.PathAsanLogo
import com.pathasan.app.ui.components.PrimaryButton
import com.pathasan.app.ui.components.SecondaryButton
import com.pathasan.app.ui.theme.PathAsanMuted
import com.pathasan.app.ui.theme.PathAsanRed

@Composable
fun WelcomeScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onSignUp: () -> Unit,
    onLogin: () -> Unit,
) {
    val fil = language == AppLanguage.FILIPINO
    var heroVisible by remember { mutableStateOf(false) }
    var authenticationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        heroVisible = true
    }

    Box(
        Modifier.fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 18.dp)
    ) {
        Row(Modifier.align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Language, null, tint = PathAsanRed)
            TextButton(onClick = {
                onLanguageChange(
                    if (language == AppLanguage.ENGLISH) AppLanguage.FILIPINO
                    else AppLanguage.ENGLISH
                )
            }) {
                Text(if (fil) "Filipino" else "English")
            }
        }

        AnimatedVisibility(
            visible = heroVisible,
            enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 12 },
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PathAsanLogo(
                    Modifier.size(
                        width = if (authenticationVisible) 210.dp else 270.dp,
                        height = if (authenticationVisible) 145.dp else 190.dp,
                    )
                        .semantics {
                            contentDescription = if (fil) {
                                "Buksan ang Sign Up at Log In"
                            } else {
                                "Open Sign Up and Log In"
                            }
                        }
                        .clickable(role = Role.Button) {
                            authenticationVisible = true
                        }
                )
                Text(
                    stringResource(R.string.tagline),
                    color = PathAsanRed,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    if (fil) "Kahandaan at impormasyon para sa Baguio City"
                    else "Preparedness and information for Baguio City",
                    color = PathAsanMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = authenticationVisible,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 8 },
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(stringResource(R.string.sign_up), onSignUp)
                SecondaryButton(stringResource(R.string.log_in), onLogin)
                Text(
                    if (fil) {
                        "Lokal lamang sa device ang kasalukuyang authentication at hindi konektado sa production server."
                    } else {
                        "Current authentication is local to this device and is not connected to a production server."
                    },
                    color = PathAsanMuted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
