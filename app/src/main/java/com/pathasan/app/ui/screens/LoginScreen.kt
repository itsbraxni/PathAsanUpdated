package com.pathasan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pathasan.app.R
import com.pathasan.app.data.AppLanguage
import com.pathasan.app.data.UserSession
import com.pathasan.app.ui.components.DemoNotice
import com.pathasan.app.ui.components.FormError
import com.pathasan.app.ui.components.PathAsanScaffold
import com.pathasan.app.ui.components.PathAsanTextField
import com.pathasan.app.ui.components.PrimaryButton
import com.pathasan.app.ui.components.validateEmail
import com.pathasan.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    language: AppLanguage,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignUp: () -> Unit,
    onForgot: () -> Unit,
    onSuccess: (UserSession) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val fil = language == AppLanguage.FILIPINO

    PathAsanScaffold(title = stringResource(R.string.log_in), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(if (fil) "Maligayang pagbabalik" else "Welcome back", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(if (fil) "Mag-log in para magpatuloy sa safety dashboard." else "Log in to continue to your safety dashboard.")
            PathAsanTextField(email, { email = it }, stringResource(R.string.email), keyboardType = KeyboardType.Email)
            PathAsanTextField(password, { password = it }, stringResource(R.string.password), password = true)
            TextButton(onClick = onForgot) { Text(stringResource(R.string.forgot_password)) }
            (localError ?: state.error)?.let { FormError(it) }
            PrimaryButton(
                if (state.loading) (if (fil) "Sandali…" else "Please wait…") else stringResource(R.string.log_in),
                enabled = !state.loading,
                onClick = {
                    localError = when {
                        !validateEmail(email) -> if (fil) "Pakilagay ang wastong email." else "Please enter a valid email."
                        password.length < 6 -> if (fil) "Hindi bababa sa 6 na character ang password." else "Password must contain at least 6 characters."
                        else -> null
                    }
                    if (localError == null) viewModel.login(email, password, onSuccess)
                },
            )
            TextButton(onClick = onSignUp) { Text(if (fil) "Bago sa PathAsan? Gumawa ng account" else "New to PathAsan? Create account") }
            DemoNotice(if (fil) "Demo authentication lamang ito at hindi server account." else "This is local demo authentication, not a server account.")
        }
    }
}

@Composable
fun ForgotPasswordScreen(language: AppLanguage, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val fil = language == AppLanguage.FILIPINO
    PathAsanScaffold(title = if (fil) "I-reset ang password" else "Reset password", onBack = onBack) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (sent) {
                Text(if (fil) "Na-simulate ang reset instructions." else "Reset instructions were simulated.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                DemoNotice(if (fil) "Walang email na ipinadala dahil walang authentication backend." else "No email was sent because no authentication backend is connected.")
                PrimaryButton(stringResource(R.string.back), onBack)
            } else {
                Text(if (fil) "Ilagay ang iyong email." else "Enter your email address.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                PathAsanTextField(email, { email = it }, stringResource(R.string.email), keyboardType = KeyboardType.Email)
                error?.let { FormError(it) }
                PrimaryButton(if (fil) "Ipadala ang Reset Link" else "Send Reset Link", onClick = {
                    if (!validateEmail(email)) error = if (fil) "Pakilagay ang wastong email." else "Please enter a valid email."
                    else sent = true
                })
            }
        }
    }
}
