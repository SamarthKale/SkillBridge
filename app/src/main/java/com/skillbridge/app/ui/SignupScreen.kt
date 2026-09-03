package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.auth.SignupPrefs
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.theme.Mute

@Composable
fun SignupScreen(
    onSignedUp: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    ScreenScaffold(title = "Create Account", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Students apply to projects. Clients post them.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )

            AppTextField(name, { name = it; error = null }, "Full Name")
            AppTextField(email, { email = it; error = null }, "Email", keyboardType = KeyboardType.Email)
            AppTextField(password, { password = it; error = null }, "Password", isPassword = true)
            AppTextField(confirm, { confirm = it; error = null }, "Confirm Password", isPassword = true)

            Text("I am a", style = MaterialTheme.typography.labelMedium, color = Mute)
            SelectableChips(
                options = listOf("student", "client"),
                selected = setOf(role),
                onToggle = { role = it }
            )

            ErrorText(error)

            PrimaryButton(
                text = "Create Account",
                loading = loading,
                onClick = {
                    error = validate(name, email, password, confirm)
                    if (error == null) {
                        loading = true
                        SignupPrefs.role = role
                        AuthManager.signUp(name, email, password) { err ->
                            loading = false
                            if (err == null) onSignedUp() else error = err
                        }
                    }
                }
            )
        }
    }
}

private fun validate(name: String, email: String, password: String, confirm: String): String? = when {
    name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank() -> "Please fill in all fields."
    password.length < 6 -> "Password should be at least 6 characters."
    password != confirm -> "Passwords do not match."
    else -> null
}
