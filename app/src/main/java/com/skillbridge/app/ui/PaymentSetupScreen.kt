package com.skillbridge.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.skillbridge.app.data.Repo
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.Mute

@Composable
fun PaymentSetupScreen(
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val uid = AuthManager.uid()

    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    fun save() {
        if (cardName.isBlank() || cardNumber.isBlank() || cardExpiry.isBlank() || cardCvv.isBlank()) {
            error = "Please fill out all payment details."
            return
        }
        if (!acceptedTerms) {
            error = "You must accept the Terms and Conditions to proceed."
            return
        }
        saving = true
        Repo.updatePaymentInfo(
            uid = uid,
            cardName = cardName.trim(),
            cardNumber = cardNumber.trim(),
            cardExpiry = cardExpiry.trim(),
            cardCvv = cardCvv.trim()
        ) { ok ->
            saving = false
            if (ok) onDone() else error = "Could not save payment information. Try again."
        }
    }

    ScreenScaffold(
        title = "Payment Details",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Please provide a payment method for verification and billing.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mute
            )

            SectionHeader("Credit or Debit Card")
            AppTextField(cardName, { cardName = it; error = null }, "Name on Card")
            AppTextField(cardNumber, { cardNumber = it; error = null }, "Card Number", keyboardType = KeyboardType.Number)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(cardExpiry, { cardExpiry = it; error = null }, "Expiry (MM/YY)", modifier = Modifier.weight(1f))
                AppTextField(cardCvv, { cardCvv = it; error = null }, "CVV", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it; error = null }
                )
                Text(
                    text = "I accept the Terms and Conditions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Accent,
                    modifier = Modifier.clickable { showTermsDialog = true }.padding(4.dp)
                )
            }

            ErrorText(error)
            Spacer(Modifier.height(4.dp))
            
            PrimaryButton(
                text = "Save & Continue",
                loading = saving,
                onClick = { save() }
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms and Conditions") },
            text = { 
                Text(
                    "We collect your payment information solely for identity verification and future billing purposes. " +
                    "By adding a card, you agree to our platform's terms of service. You will only be charged when you " +
                    "authorize a payment or hire a freelancer.\n\n" +
                    "Your card details are stored securely and will not be shared with third parties."
                ) 
            },
            confirmButton = { 
                TextButton(
                    onClick = { 
                        acceptedTerms = true
                        error = null
                        showTermsDialog = false 
                    }
                ) { Text("I Agree") } 
            },
            dismissButton = { 
                TextButton(onClick = { showTermsDialog = false }) { Text("Close") } 
            }
        )
    }
}
