package com.skillbridge.app.ui

import androidx.compose.foundation.clickable
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
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.Mute

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
    ) {
        Text("SkillBridge", style = MaterialTheme.typography.titleLarge)
        Text("Log in to find your next opportunity.", style = MaterialTheme.typography.bodyMedium, color = Mute)

        AppTextField(email, { email = it; message = null }, "Email", keyboardType = KeyboardType.Email)
        AppTextField(password, { password = it; message = null }, "Password", isPassword = true)

        Text(
            "Forgot Password?",
            style = MaterialTheme.typography.labelMedium,
            color = Accent,
            modifier = Modifier.clickable {
                AuthManager.sendPasswordReset(email) { err ->
                    message = err ?: "Password reset email sent to $email."
                }
            }
        )

        ErrorText(message)

        PrimaryButton(
            text = "Login",
            loading = loading,
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    message = "Please fill in both fields."
                } else {
                    loading = true
                    AuthManager.login(email, password) { err ->
                        loading = false
                        if (err == null) onLoggedIn() else message = err
                    }
                }
            }
        )
        SecondaryButton(text = "Sign Up", enabled = !loading, onClick = onCreateAccount)
    }
}
