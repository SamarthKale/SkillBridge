package com.skillbridge.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
    val uid = AuthManager.uid()

    var loaded by remember { mutableStateOf(!isEdit) }
    var existing by remember { mutableStateOf<Profile?>(null) }

    var name by remember { mutableStateOf(AuthManager.displayName()) }
    val email = AuthManager.email()
    var phone by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var gradYear by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf(setOf<String>()) }
    var experience by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var resumeUrl by remember { mutableStateOf("") }
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
                    degree = p.degree
                    college = p.college
                    branch = p.branch
                    gradYear = if (p.graduationYear > 0) p.graduationYear.toString() else ""
                    skills = p.skills.toSet()
                    experience = p.experience
                    bio = p.bio
                    resumeUrl = p.resumeUrl
                }
                loaded = true
            }
        }
    }

    fun save() {
        if (name.isBlank() || college.isBlank() || skills.isEmpty()) {
            error = "Name, college and at least one skill are required."
            return
        }
        saving = true
        val now = System.currentTimeMillis()
        val profile = Profile(
            uid = uid,
            role = existing?.role ?: SignupPrefs.role,
            name = name.trim(),
            email = email,
            phone = phone.trim(),
            location = location.trim(),
            photoUrl = existing?.photoUrl ?: "",
            degree = degree.trim(),
            college = college.trim(),
            branch = branch.trim(),
            graduationYear = gradYear.trim().toIntOrNull() ?: 0,
            skills = skills.toList(),
            experience = experience.trim(),
            bio = bio.trim(),
            resumeUrl = resumeUrl.trim(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        Repo.saveProfile(profile) { ok ->
            if (ok) onDone() else { saving = false; error = "Could not save your profile. Try again." }
        }
    }

    ScreenScaffold(
        title = if (isEdit) "Edit Profile" else "Set Up Your Profile",
        onBack = if (isEdit) onBack else null
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
