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
import com.skillbridge.app.data.ResumeFile
import com.skillbridge.app.data.Skills
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.utils.FileUtils
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton
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
    var photoUrl by remember { mutableStateOf("") }
    var existingResume by remember { mutableStateOf<ResumeFile?>(null) }
    var newResume by remember { mutableStateOf<ResumeFile?>(null) }
    var resumeRemoved by remember { mutableStateOf(false) }
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
                    photoUrl = p.photoUrl
                    companyName = p.companyName
                    companyType = p.companyType
                    companyDescription = p.companyDescription
                }
                loaded = true
            }
            Repo.getResume(uid) { existingResume = it }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val encoded = FileUtils.encodePhoto(context, uri)
            if (encoded != null) { photoUrl = encoded; error = null } else error = "Could not read that image."
        }
    }
    val resumePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            when (val r = FileUtils.readResume(context, uri, uid)) {
                is FileUtils.ResumeRead.Ok -> { newResume = r.file; resumeRemoved = false; error = null }
                is FileUtils.ResumeRead.Error -> error = r.message
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
            photoUrl = photoUrl,
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
            if (!ok) { saving = false; error = "Could not save your profile. Try again."; return@saveProfile }
            // The optional resume file is saved after the profile; a failure here keeps the user on the form.
            val upload = newResume
            fun resumeDone(success: Boolean) {
                if (success) onDone() else { saving = false; error = "Profile saved, but the resume file could not be uploaded." }
            }
            when {
                upload != null -> Repo.saveResume(upload, ::resumeDone)
                resumeRemoved && existingResume != null -> Repo.deleteResume(uid, ::resumeDone)
                else -> onDone()
            }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(name.ifBlank { "?" }, size = 72, photo = photoUrl)
                Spacer(Modifier.width(16.dp))
                Column {
                    SecondaryButton(
                        if (photoUrl.isBlank()) "Add photo" else "Change photo",
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    if (photoUrl.isNotBlank()) {
                        TextButton(onClick = { photoUrl = "" }) { Text("Remove photo") }
                    }
                }
            }

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
                SectionHeader("Resume (optional)")
                AppTextField(resumeUrl, { resumeUrl = it }, "Link to your resume (Google Drive, etc.)")

                val shownResume = newResume ?: existingResume.takeUnless { resumeRemoved }
                if (shownResume != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("📄 ${shownResume.fileName}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                FileUtils.formatSize(shownResume.sizeBytes) +
                                    if (newResume != null) " · uploads when you save" else "",
                                style = MaterialTheme.typography.labelMedium, color = Mute
                            )
                        }
                        TextButton(onClick = { newResume = null; resumeRemoved = true }) { Text("Remove") }
                    }
                }
                SecondaryButton(
                    if (shownResume == null) "Upload resume file" else "Replace resume file",
                    onClick = { resumePicker.launch(FileUtils.RESUME_MIME_TYPES) }
                )
                Text(
                    "PDF or Word, up to ${FileUtils.MAX_RESUME_BYTES / 1024} KB. You can skip this.",
                    style = MaterialTheme.typography.labelMedium, color = Mute
                )
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
