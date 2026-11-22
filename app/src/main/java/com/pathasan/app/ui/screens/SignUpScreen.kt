package com.pathasan.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
fun SignUpScreen(
    language: AppLanguage,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSuccess: (UserSession) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val fil = language == AppLanguage.FILIPINO

    PathAsanScaffold(title = stringResource(R.string.sign_up), onBack = onBack) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(if (fil) "Gumawa ng account" else "Create your account", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(if (fil) "Mag-set up ng lokal na demo profile." else "Set up a local demonstration profile.")
            PathAsanTextField(name, { name = it }, stringResource(R.string.full_name))
            PathAsanTextField(email, { email = it }, stringResource(R.string.email), keyboardType = KeyboardType.Email)
            PathAsanTextField(password, { password = it }, stringResource(R.string.password), password = true)
            PathAsanTextField(confirm, { confirm = it }, stringResource(R.string.confirm_password), password = true)
            (localError ?: state.error)?.let { FormError(it) }
            PrimaryButton(
                text = if (state.loading) (if (fil) "Sandali…" else "Please wait…") else stringResource(R.string.sign_up),
                enabled = !state.loading,
                onClick = {
                    localError = when {
                        name.trim().length < 2 -> if (fil) "Pakilagay ang buong pangalan." else "Please enter your full name."
                        !validateEmail(email) -> if (fil) "Pakilagay ang wastong email." else "Please enter a valid email."
                        password.length < 6 -> if (fil) "Hindi bababa sa 6 na character ang password." else "Password must contain at least 6 characters."
                        password != confirm -> if (fil) "Hindi magkatugma ang password." else "Passwords do not match."
                        else -> null
                    }
                    if (localError == null) viewModel.signUp(name, email, password, onSuccess)
                },
            )
            TextButton(onClick = onLogin) {
                Text(if (fil) "May account ka na? Mag-log In" else "Already have an account? Log In")
            }
            DemoNotice(if (fil) "Lokal lamang ang account na ito. Kailangan ng secure backend para sa production." else "This account exists only on this device. Production requires a secure authentication backend.")
        }
    }
}
