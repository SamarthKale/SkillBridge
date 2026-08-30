package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.theme.Mute

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Text("SkillBridge", style = MaterialTheme.typography.titleLarge)
        Text("Freelance projects for college students", style = MaterialTheme.typography.bodyMedium, color = Mute)

        AppTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = "Email",
            keyboardType = KeyboardType.Email
        )
        AppTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = "Password",
            isPassword = true
        )

        ErrorText(error)

        PrimaryButton(
            text = "Login",
            loading = loading,
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> error = "Please fill in both fields."
                    else -> {
                        loading = true
                        AuthManager.login(email, password) { err ->
                            loading = false
                            if (err == null) onLoggedIn() else error = err
                        }
                    }
                }
            }
        )
        SecondaryButton(
            text = "Create Account",
            enabled = !loading,
            onClick = onCreateAccount
        )
    }
}
