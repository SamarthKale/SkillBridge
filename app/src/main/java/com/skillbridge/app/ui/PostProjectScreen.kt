package com.skillbridge.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skillbridge.app.auth.AuthManager
import com.skillbridge.app.data.Category
import com.skillbridge.app.data.Opportunity
import com.skillbridge.app.data.Repo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.skillbridge.app.data.Skills
import com.skillbridge.app.data.WorkArrangement
import com.skillbridge.app.utils.LocationUtils
import com.skillbridge.app.ui.components.AppTextField
import com.skillbridge.app.ui.components.ErrorText
import com.skillbridge.app.ui.components.PrimaryButton
import com.skillbridge.app.ui.components.SecondaryButton
import com.skillbridge.app.ui.components.ScreenScaffold
import com.skillbridge.app.ui.components.SectionHeader
import com.skillbridge.app.ui.components.SelectableChips
import com.skillbridge.app.ui.components.rememberMyProfile
import com.skillbridge.app.ui.theme.Accent
import com.skillbridge.app.ui.theme.Ink
import com.skillbridge.app.ui.theme.Line
import com.skillbridge.app.ui.theme.Mute
import com.skillbridge.app.ui.theme.Paper
import com.skillbridge.app.ui.theme.Smoke
import java.util.Locale

@Composable
fun PostProjectScreen(
    onProjectPosted: () -> Unit
) {
    val context = LocalContext.current
    val uid = AuthManager.uid()
    val profile by rememberMyProfile()

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(Category.DEVELOPMENT) }
    var budget by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var workArrangement by remember { mutableStateOf(WorkArrangement.REMOTE) }
    var maxDistanceKm by remember { mutableStateOf("0") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var selectedSkills by remember { mutableStateOf(setOf<String>()) }
    var description by remember { mutableStateOf("") }
    var responsibilities by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var experienceRequired by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.values.any { it }) {
                LocationUtils.getCurrentLocation(context, onSuccess = { lat, lon ->
                    latitude = lat
                    longitude = lon
                    error = null
                }, onFailure = {
                    error = "Could not fetch location. Please enter it manually."
                })
            } else {
                error = "Location permission denied. Please enter location manually."
            }
        }
    )

    fun post() {
        if (title.isBlank() || budget.isBlank() || description.isBlank()) {
            error = "Title, budget, and description are required."
            return
        }
        saving = true
        val opp = Opportunity(
            title = title.trim(),
            company = profile?.companyName?.ifBlank { profile?.name } ?: "Unknown Company",
            category = category,
            budget = budget.trim(),
            location = if (workArrangement == WorkArrangement.REMOTE) "" else location.trim(),
            remote = workArrangement == WorkArrangement.REMOTE,
            workArrangement = workArrangement,
            latitude = if (workArrangement == WorkArrangement.REMOTE) null else latitude,
            longitude = if (workArrangement == WorkArrangement.REMOTE) null else longitude,
            maxDistanceKm = if (workArrangement == WorkArrangement.REMOTE) 0 else maxDistanceKm.toIntOrNull() ?: 0,
            skills = selectedSkills.toList(),
            description = description.trim(),
            responsibilities = responsibilities.trim(),
            requirements = requirements.trim(),
            experienceRequired = experienceRequired.trim(),
            deadline = deadline.trim(),
            postedByUid = uid,
            postedByName = profile?.name ?: AuthManager.displayName(),
            postedAt = System.currentTimeMillis()
        )
        Repo.postOpportunity(opp) { ok ->
            saving = false
            if (ok) onProjectPosted() else error = "Failed to post project. Try again."
        }
    }

    ScreenScaffold(title = "Post a New Project") { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("Basic Details")
            AppTextField(title, { title = it; error = null }, "Project Title")
            
            Text("Category", style = MaterialTheme.typography.labelMedium)
            SelectableChips(
                options = Category.ALL,
                selected = setOf(category),
                onToggle = { category = it }
            )

            SectionHeader("Scope & Requirements")
            AppTextField(description, { description = it; error = null }, "Description", singleLine = false, minLines = 3)
            AppTextField(responsibilities, { responsibilities = it }, "Responsibilities (Optional)", singleLine = false, minLines = 2)
            AppTextField(requirements, { requirements = it }, "Requirements (Optional)", singleLine = false, minLines = 2)
            AppTextField(experienceRequired, { experienceRequired = it }, "Experience Required (Optional)")

            SectionHeader("Skills Needed")
            SelectableChips(
                options = Skills.ALL,
                selected = selectedSkills,
                onToggle = { s -> selectedSkills = if (s in selectedSkills) selectedSkills - s else selectedSkills + s }
            )

            SectionHeader("Budget & Timeline")
            AppTextField(budget, { budget = it; error = null }, "Budget (e.g. ₹5,000 or $100)")
            AppTextField(deadline, { deadline = it }, "Deadline (e.g. 2 Weeks or 24 Oct)")

            SectionHeader("Work Preferences")
            Text("Work Arrangement", style = MaterialTheme.typography.labelMedium)
            SelectableChips(
                options = WorkArrangement.ALL,
                selected = setOf(workArrangement),
                onToggle = { workArrangement = it }
            )

            if (workArrangement != WorkArrangement.REMOTE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Smoke, RoundedCornerShape(12.dp))
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Location Details ($workArrangement)",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink
                    )
                    Text(
                        text = "Attach GPS coordinates so students and freelancers can discover your project on the interactive map.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Mute
                    )

                    SecondaryButton(
                        text = if (latitude != null && longitude != null) "📍 Update GPS Coordinates" else "📍 Use Current Location (GPS)",
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )

                    if (latitude != null && longitude != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Paper, RoundedCornerShape(8.dp))
                                .border(1.dp, Line, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "📍 ${String.format(Locale.getDefault(), "%.4f", latitude)}, ${String.format(Locale.getDefault(), "%.4f", longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Accent,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Captured ✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = Accent
                            )
                        }
                    }
                }

                AppTextField(location, { location = it }, "Location Name / Address (e.g. Bandra, Mumbai)")

                AppTextField(maxDistanceKm, { maxDistanceKm = it }, "Max acceptable distance for freelancer (km)", keyboardType = KeyboardType.Number)
            }

            ErrorText(error)
            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = "Publish Project",
                loading = saving,
                onClick = { post() }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
