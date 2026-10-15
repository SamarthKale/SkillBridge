package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.skillbridge.app.data.Repo
import com.skillbridge.app.utils.LocationUtils
import com.skillbridge.app.ui.components.Avatar
import com.skillbridge.app.ui.components.LabeledValue
import com.skillbridge.app.ui.components.OutlinedCardBox
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SkillChips
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val profile by rememberMyProfile()
    var confirmLogout by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.values.any { it }) {
                LocationUtils.getCurrentLocation(context, onSuccess = { lat, lon ->
                    profile?.let { p ->
                        val updated = p.copy(locationEnabled = true, latitude = lat, longitude = lon)
                        Repo.saveProfile(updated) {}
                    }
                }, onFailure = { })
            }
        }
    )

    ScreenScaffold(title = "Profile") { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val name = profile?.name ?: AuthManager.displayName()
            val isClient = profile?.role == "client"

            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name, size = 60)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        listOfNotNull(
                            profile?.role?.replaceFirstChar { it.uppercase() },
                            if (isClient) profile?.companyName?.ifBlank { null } else profile?.branch?.ifBlank { null }
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium, color = Mute
                    )
                    Text(AuthManager.email(), style = MaterialTheme.typography.labelMedium, color = Mute)
                }
            }

            if (isClient) {
                SectionHeader("Company Details")
                OutlinedCardBox {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledValue("Company Name", profile?.companyName ?: "-")
                        LabeledValue("Type", profile?.companyType ?: "-")
                        LabeledValue("Location", profile?.location ?: "-")
                    }
                }

                SectionHeader("About Us")
                Text(profile?.companyDescription?.ifBlank { "-" } ?: "-", style = MaterialTheme.typography.bodyMedium)
            } else {
                SectionHeader("Skills")
                SkillChips(profile?.skills ?: emptyList())

                SectionHeader("Education")
                OutlinedCardBox {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledValue("Degree", profile?.degree ?: "-")
                        LabeledValue("College", profile?.college ?: "-")
                        LabeledValue("Branch", profile?.branch ?: "-")
                        LabeledValue(
                            "Graduation Year",
                            profile?.graduationYear?.takeIf { it > 0 }?.toString() ?: "-"
                        )
                    }
                }

                SectionHeader("Experience")
                Text(profile?.experience?.ifBlank { "-" } ?: "-", style = MaterialTheme.typography.bodyMedium)

                SectionHeader("About")
                Text(profile?.bio?.ifBlank { "-" } ?: "-", style = MaterialTheme.typography.bodyMedium)

                SectionHeader("Resume")
                Text(profile?.resumeUrl?.ifBlank { "No resume link added." } ?: "-",
                    style = MaterialTheme.typography.bodyMedium, color = Mute)
            }

            Spacer(Modifier.height(6.dp))
            SectionHeader("Privacy Settings")
            OutlinedCardBox {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Share my location (for nearby matches)", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(
                        checked = profile?.locationEnabled == true,
                        onCheckedChange = { enable ->
                            if (enable) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                profile?.let { p ->
                                    val updated = p.copy(locationEnabled = false, latitude = null, longitude = null)
                                    Repo.saveProfile(updated) {}
                                }
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            PrimaryButton("Edit Profile", onClick = onEditProfile)
            SecondaryButton("Logout", onClick = { confirmLogout = true })
            Spacer(Modifier.height(12.dp))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Logout?") },
            text = { Text("You will need to sign in again.") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; onLoggedOut() }) { Text("Logout") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancel") } }
        )
    }
}
