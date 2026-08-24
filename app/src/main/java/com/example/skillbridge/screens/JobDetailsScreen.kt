package com.example.skillbridge.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(onBackClick: () -> Unit = {}, onApplyClick: () -> Unit = {}) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            JobDetailsBottomBar(onApplyClick = onApplyClick)
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
            // Header Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(IconBgBlue, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Logo Designer for Startup",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "TechFlow Inc.", color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = BluePrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Info Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.Payments,
                    label = "$25/hr",
                    subLabel = "Compensation",
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    icon = Icons.Default.LocationOn,
                    label = "0.5 mi",
                    subLabel = "Palo Alto, CA",
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    icon = Icons.Default.Schedule,
                    label = "Flexible",
                    subLabel = "Hours",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Job Description Section
            DetailsSection(title = "Job Description") {
                Text(
                    text = "We're looking for a creative student designer to help us craft a modern and impactful logo for our new fintech startup. You'll work directly with the founders to translate our vision into a visual identity that resonates with young professionals.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Requirements Section
            DetailsSection(title = "Requirements") {
                RequirementItem("Proficiency in Adobe Illustrator or Figma")
                RequirementItem("Strong portfolio with branding examples")
                RequirementItem("Ability to meet weekly milestones and iterate based on feedback")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagChip("Figma")
                    TagChip("Illustrator")
                    TagChip("Branding")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Project Milestones Section
            DetailsSection(title = "Project Milestones") {
                Column {
                    MilestoneItem(
                        number = 1,
                        title = "Concept Exploration",
                        description = "Initial mood boards and rough sketches.",
                        week = "Week 1",
                        isFirst = true
                    )
                    MilestoneItem(
                        number = 2,
                        title = "Initial Drafts",
                        description = "3-5 polished vector concepts.",
                        week = "Week 2"
                    )
                    MilestoneItem(
                        number = 3,
                        title = "Revisions & Handoff",
                        description = "Final tweaks and asset delivery.",
                        week = "Week 3",
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About the Client Section
            DetailsSection(title = "About the Client") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.LightGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("TechFlow Inc.", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Fintech Startup innovating payment solutions for Gen Z.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, subLabel: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subLabel, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DetailsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun RequirementItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun TagChip(text: String) {
    Surface(
        color = Color(0xFFECFDF5),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = Color(0xFF10B981)
        )
    }
}

@Composable
fun MilestoneItem(
    number: Int,
    title: String,
    description: String,
    week: String,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(if (number == 1) BluePrimary else Color.White, CircleShape)
                    .border(1.dp, if (number == 1) BluePrimary else Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = if (number == 1) Color.White else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Color.LightGray)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(week, fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
fun JobDetailsBottomBar(onApplyClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BluePrimary)
            ) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Message", color = BluePrimary)
            }
            Button(
                onClick = onApplyClick,
                modifier = Modifier.weight(1.5f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Apply Now", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JobDetailsScreenPreview() {
    SkillBridgeTheme {
        JobDetailsScreen()
    }
}
