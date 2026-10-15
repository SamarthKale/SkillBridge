package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.skillbridge.app.utils.LocationUtils
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.auth.SignupPrefs
import com.skillbridge.app.data.Profile
import com.skillbridge.app.data.Repo
import com.skillbridge.app.data.Skills
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.Avatar
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.theme.Mute

@Composable
fun ProfileSetupScreen(
    isEdit: Boolean,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uid = AuthManager.uid()

    var loaded by remember { mutableStateOf(!isEdit) }
    var existing by remember { mutableStateOf<Profile?>(null) }

    var name by remember { mutableStateOf(AuthManager.displayName()) }
    val email = AuthManager.email()
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var locationEnabled by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var degree by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var gradYear by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(setOf<String>()) }
    var experience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var companyType by remember { mutableStateOf("") }
    var companyDescription by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isEdit) {
            Repo.getProfile(uid) { p ->
                if (p != null) {
                    existing = p
                    name = p.name
                    phone = p.phone
                    location = p.location
                    locationEnabled = p.locationEnabled
                    latitude = p.latitude
                    longitude = p.longitude
                    degree = p.degree
                    college = p.college
                    branch = p.branch
                    gradYear = if (p.graduationYear > 0) p.graduationYear.toString() else ""
                    skills = p.skills.toSet()
                    experience = p.experience
                    bio = p.bio
                    resumeUrl = p.resumeUrl
                    companyName = p.companyName
                    companyType = p.companyType
                    companyDescription = p.companyDescription
                }
                loaded = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.values.any { it }) {
                LocationUtils.getCurrentLocation(context, onSuccess = { lat, lon ->
                    latitude = lat
                    longitude = lon
                }, onFailure = {
                    error = "Could not fetch location."
                    locationEnabled = false
                })
            } else {
                error = "Location permission denied."
                locationEnabled = false
            }
        }
    )

    fun save() {
        val roleValue = existing?.role ?: SignupPrefs.role
        if (roleValue == "student") {
            if (name.isBlank() || college.isBlank() || skills.isEmpty()) {
                error = "Name, college and at least one skill are required."
                return
            }
        } else {
            if (name.isBlank() || companyName.isBlank()) {
                error = "Name and Company Name are required."
                return
            }
        }
        saving = true
        val now = System.currentTimeMillis()
        val profile = Profile(
            uid = uid,
            role = roleValue,
            name = name.trim(),
            email = email,
            phone = phone.trim(),
            location = location.trim(),
            locationEnabled = locationEnabled,
            latitude = latitude,
            longitude = longitude,
            locationUpdatedAt = if (locationEnabled && latitude != null) now else existing?.locationUpdatedAt ?: 0L,
            photoUrl = existing?.photoUrl ?: "",
            degree = degree.trim(),
            college = college.trim(),
            branch = branch.trim(),
            graduationYear = gradYear.trim().toIntOrNull() ?: 0,
            skills = skills.toList(),
            experience = experience.trim(),
            bio = bio.trim(),
            resumeUrl = resumeUrl.trim(),
            companyName = companyName.trim(),
            companyType = companyType.trim(),
            companyDescription = companyDescription.trim(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        Repo.saveProfile(profile) { ok ->
            if (ok) onDone() else { saving = false; error = "Could not save your profile. Try again." }
        }
    }

    ScreenScaffold(
        title = if (isEdit) "Edit Profile" else "Set Up Your Profile",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar(name.ifBlank { "?" }, size = 64)
            Text("Photo upload is disabled in this build.", style = MaterialTheme.typography.labelMedium, color = Mute)

            SectionHeader("Personal")
            AppTextField(name, { name = it; error = null }, "Name")
            AppTextField(email, {}, "Email")
            AppTextField(phone, { phone = it }, "Phone", keyboardType = KeyboardType.Phone)
            AppTextField(location, { location = it }, "Location")

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Switch(
                    checked = locationEnabled,
                    onCheckedChange = { 
                        locationEnabled = it 
                        if (it && latitude == null) {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else if (!it) {
                            latitude = null
                            longitude = null
                        }
                    }
                )
                Text(
                    "Enable Location Sharing",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            val roleValue = existing?.role ?: SignupPrefs.role

            if (roleValue == "student") {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Education")
                AppTextField(degree, { degree = it }, "Degree (e.g. B.Tech)")
                AppTextField(college, { college = it; error = null }, "College / University")
                AppTextField(branch, { branch = it }, "Branch / Specialization")
                AppTextField(gradYear, { gradYear = it }, "Graduation Year", keyboardType = KeyboardType.Number)

                Spacer(Modifier.height(4.dp))
                SectionHeader("Skills")
                SelectableChips(
                    options = Skills.ALL,
                    selected = skills,
                    onToggle = { s -> skills = if (s in skills) skills - s else skills + s }
                )

                Spacer(Modifier.height(4.dp))
                SectionHeader("Experience")
                AppTextField(experience, { experience = it }, "Projects, internships, previous work",
                    singleLine = false, minLines = 3)

                Spacer(Modifier.height(4.dp))
                SectionHeader("About Me")
                AppTextField(bio, { bio = it }, "Short bio", singleLine = false, minLines = 2)

                Spacer(Modifier.height(4.dp))
                SectionHeader("Resume")
                AppTextField(resumeUrl, { resumeUrl = it }, "Link to your resume (Google Drive, etc.)")
            } else {
                Spacer(Modifier.height(4.dp))
                SectionHeader("Company Details")
                AppTextField(companyName, { companyName = it; error = null }, "Company Name")
                AppTextField(companyType, { companyType = it }, "Type of Company (e.g. Startup, Agency)")
                
                Spacer(Modifier.height(4.dp))
                SectionHeader("What We Do")
                AppTextField(companyDescription, { companyDescription = it }, "Description of your company", singleLine = false, minLines = 3)
            }

            ErrorText(error)
            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = if (isEdit) "Save Changes" else "Save & Continue",
                loading = saving,
                enabled = loaded,
                onClick = { save() }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
