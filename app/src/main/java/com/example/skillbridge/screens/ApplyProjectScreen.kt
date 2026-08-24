package com.example.skillbridge.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyProjectScreen(onBackClick: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    var introText by remember { mutableStateOf("Hi TechFlow team, I'm a senior design student with a passion for startup branding...") }
    var compensation by remember { mutableStateOf("25") }
    var selectedTimeline by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply for Project", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Submit Application", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Job Header Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(IconBgBlue, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = BluePrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Logo Designer for Startup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("TechFlow Inc. • Remote", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Introduce Yourself
            SectionHeader("Introduce Yourself", "Why are you a good fit for this role?")
            OutlinedTextField(
                value = introText,
                onValueChange = { introText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Relevant Experience
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Relevant Experience", "Select portfolio projects to highlight", modifier = Modifier.weight(1f))
                TextButton(onClick = {}) {
                    Text("Add New", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    PortfolioCard(
                        title = "Fintech Rebrand",
                        subtitle = "Branding • 2023",
                        isSelected = true
                    )
                }
                item {
                    PortfolioCard(
                        title = "Eco Startup Logo",
                        subtitle = "Logo Design • 2023",
                        isSelected = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Your Proposed Timeline
            SectionHeader("Your Proposed Timeline", "The client requested a 3-week milestone schedule (Concept, Drafts, Handoff).")
            TimelineOption(
                text = "I can meet the 3-week schedule",
                isSelected = selectedTimeline == 0,
                onClick = { selectedTimeline = 0 }
            )
            Spacer(modifier = Modifier.height(12.dp))
            TimelineOption(
                text = "I need to propose an alternative",
                isSelected = selectedTimeline == 1,
                onClick = { selectedTimeline = 1 }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Estimated Compensation
            SectionHeader("Estimated Compensation (Hourly)")
            OutlinedTextField(
                value = compensation,
                onValueChange = { compensation = it },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$ ", color = Color.Gray) },
                suffix = { Text("/ hr", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Attachments
            SectionHeader("Attachments")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(
                        BorderStroke(
                            1.dp,
                            color = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Upload Resume or Portfolio PDF", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(bottom = 12.dp)) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        if (subtitle != null) {
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PortfolioCard(title: String, subtitle: String, isSelected: Boolean) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) BluePrimary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.TopEnd
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TimelineOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) BluePrimary else Color(0xFFE2E8F0)),
        color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by Surface
                colors = RadioButtonDefaults.colors(selectedColor = BluePrimary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, fontSize = 14.sp, color = if (isSelected) Color.Black else Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ApplyProjectScreenPreview() {
    SkillBridgeTheme {
        ApplyProjectScreen()
    }
}
