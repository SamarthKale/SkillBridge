package com.example.skillbridge.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.ui.theme.*

data class Job(
    val title: String,
    val company: String,
    val pay: String,
    val distance: String,
    val tags: List<String>,
    val isVerified: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onJobClick: (Job) -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val jobs = listOf(
        Job("Logo Designer for Startup", "TechFlow Inc.", "$25/hr", "0.5 miles away", listOf("Design", "Student-led", "Flexible"), true),
        Job("Python Tutor Needed", "Sarah Jenkins", "$30/hr", "1.2 miles away", listOf("Coding", "Tutoring", "Remote Option"), true),
        Job("Social Media Manager", "Local Coffee Roasters", "$20/hr", "0.8 miles away", listOf("Marketing", "On-site", "Part-time")),
        Job("Frontend Developer", "WebApps Studio", "$35/hr", "2.5 miles away", listOf("Coding", "Remote", "Flexible"), true),
        Job("Graphic Design Intern", "Creative Pulse", "$15/hr", "1.0 miles away", listOf("Design", "Internship", "On-site")),
        Job("Content Writer", "Blogify", "$22/hr", "5.0 miles away", listOf("Writing", "Remote", "Part-time")),
        Job("Data Entry", "QuickJobs", "$18/hr", "3.2 miles away", listOf("Admin", "Remote", "Part-time")),
        Job("App Developer", "DevX", "$45/hr", "1.5 miles away", listOf("Coding", "Android", "Full-time"), true)
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "explore",
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header: Menu and Search
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                    Spacer(modifier = Modifier.width(16.dp))
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search jobs, skills...", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariant,
                            unfocusedContainerColor = SurfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = Color.Black) }
                    )
                }
            }

            item {
                // Location Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E7FF))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF818CF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Showing jobs near", fontSize = 12.sp, color = Color.Gray)
                            Text("Stanford University", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            item {
                // Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("All Jobs", "Graphic Design", "Coding", "Marketing")) { filter ->
                        val isSelected = filter == "All Jobs"
                        Surface(
                            color = if (isSelected) BluePrimary else ChipUnselected,
                            shape = RoundedCornerShape(20.dp),
                            onClick = {}
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Nearby Opportunities",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Jobs List
            items(jobs) { job ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    JobCard(job, onClick = { onJobClick(job) })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun JobCard(job: Job, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(job.company, color = Color.Gray, fontSize = 14.sp)
                        if (job.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(job.pay, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.NearMe, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(job.distance, color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                job.tags.forEach { tag ->
                    Surface(
                        color = if (tag == "Design" || tag == "Coding") Color(0xFFECFDF5) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            tag,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = if (tag == "Design" || tag == "Coding") Color(0xFF10B981) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: String = "explore",
    onExploreClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onInboxClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == "explore",
            onClick = onExploreClick,
            icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = currentScreen == "applied",
            onClick = onAppliedClick,
            icon = { Icon(Icons.Default.Assignment, contentDescription = "Applied") },
            label = { Text("Applied") }
        )
        NavigationBarItem(
            selected = currentScreen == "inbox",
            onClick = onInboxClick,
            icon = { Icon(Icons.Default.Mail, contentDescription = "Inbox") },
            label = { Text("Inbox") }
        )
        NavigationBarItem(
            selected = currentScreen == "profile",
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExploreScreenPreview() {
    SkillBridgeTheme {
        ExploreScreen()
    }
}
