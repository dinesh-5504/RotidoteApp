package com.rotidote.app.ui.screens.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
// import androidx.lifecycle.lifecycleScope
// import kotlinx.coroutines.launch
import com.rotidote.app.ui.viewmodels.AuthViewModel
import com.rotidote.app.ui.viewmodels.AdminViewModel
import com.rotidote.app.data.models.User
import com.rotidote.app.data.models.Session
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSessionDetail: (Session) -> Unit,
    initialTab: String = "students",
    authViewModel: AuthViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    // Map tab string to index
    val tabIndexMap = mapOf("students" to 0, "sessions" to 1, "upload" to 2, "analytics" to 3)
    var selectedTab by remember { mutableIntStateOf(tabIndexMap[initialTab] ?: 0) }
    val tabs = listOf(
        AdminTab("Students", Icons.Default.Group),
        AdminTab("Sessions", Icons.Default.PlayCircle),
        AdminTab("Upload", Icons.Default.CloudUpload),
        AdminTab("Analytics", Icons.Default.Analytics)
    )
    
    Scaffold(
        modifier = Modifier.background(Color.Black),
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                actions = {
                    // Debug button for manual token fetch

                    IconButton(
                        onClick = {
                            authViewModel.signOut()
                            onNavigateToLogin()
                        }
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { 
                            Icon(
                                tab.icon, 
                                contentDescription = tab.title,
                                tint = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                            ) 
                        },
                        label = { 
                            Text(
                                tab.title,
                                color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.6f)
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> AdminStudentsContent(adminViewModel)
                1 -> AdminSessionsContent(adminViewModel, onNavigateToSessionDetail)
                2 -> AdminUploadContent(adminViewModel)
                3 -> AdminAnalyticsContent()
            }
        }
    }
}

@Composable
private fun AdminStudentsContent(adminViewModel: AdminViewModel) {
    val filteredStudents by adminViewModel.filteredStudents.collectAsState()
    val searchQuery by adminViewModel.searchQuery.collectAsState()
    val selectedSchool by adminViewModel.selectedSchool.collectAsState()
    val selectedGrade by adminViewModel.selectedGrade.collectAsState()
    val selectedSection by adminViewModel.selectedSection.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Students Management (${filteredStudents.size})",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = adminViewModel::setSearchQuery,
            label = { Text("Search students...", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { adminViewModel.setSchoolFilter(null) },
                    label = { Text("All Schools") },
                    selected = selectedSchool == null
                )
            }
            items(adminViewModel.getUniqueSchools()) { school ->
                FilterChip(
                    onClick = { adminViewModel.setSchoolFilter(school) },
                    label = { Text(school) },
                    selected = selectedSchool == school
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(adminViewModel.getUniqueGrades()) { grade ->
                FilterChip(
                    onClick = { adminViewModel.setGradeFilter(if (selectedGrade == grade) null else grade) },
                    label = { Text("Grade $grade") },
                    selected = selectedGrade == grade
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Section Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(adminViewModel.getUniqueSections()) { section ->
                FilterChip(
                    onClick = { adminViewModel.setSectionFilter(if (selectedSection == section) null else section) },
                    label = { Text("Section $section") },
                    selected = selectedSection == section
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Students List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredStudents) { student ->
                    StudentCard(student = student)
                }
            }
        }
    }
}

@Composable
private fun AdminSessionsContent(
    adminViewModel: AdminViewModel,
    onNavigateToSessionDetail: (Session) -> Unit
) {
    val sessions by adminViewModel.sessions.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sessions Management",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Days List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..5).map { "Day$it" }) { dayId ->
                val session = sessions.find { it.dayId == dayId }
                SessionCard(
                    dayId = dayId,
                    session = session,
                    onToggleEnabled = { enabled ->
                        adminViewModel.toggleSessionEnabled(dayId, enabled)
                    },
                    onSessionClick = {
                        if (session != null && session.enabled) {
                            onNavigateToSessionDetail(session)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminUploadContent(adminViewModel: AdminViewModel) {
    val uploadTitle by adminViewModel.uploadTitle.collectAsState()
    val uploadDescription by adminViewModel.uploadDescription.collectAsState()
    val selectedDay by adminViewModel.selectedDay.collectAsState()
    val selectedVideoFile by adminViewModel.selectedVideoFile.collectAsState()
    val selectedThumbnailFile by adminViewModel.selectedThumbnailFile.collectAsState()
    val uploadProgress by adminViewModel.uploadProgress.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Upload Video",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Upload Form
        OutlinedTextField(
            value = uploadTitle,
            onValueChange = adminViewModel::setUploadTitle,
            label = { Text("Video Title", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Duration Input
        OutlinedTextField(
            value = adminViewModel.uploadDuration.collectAsState().value.toString(),
            onValueChange = { value ->
                val duration = value.toIntOrNull() ?: 0
                adminViewModel.setUploadDuration(duration)
            },
            label = { Text("Duration (seconds)", color = Color.White.copy(alpha = 0.7f)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // File Selection Section
        Text(
            text = "Files:",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Video File Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Video File",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = selectedVideoFile ?: "No file selected",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        // TODO: Implement real file picker - for now using placeholder
                        adminViewModel.setSelectedVideoFile("sample_video.mp4")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Select Video", color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Thumbnail File Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thumbnail Image",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = selectedThumbnailFile ?: "No file selected",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        // TODO: Implement real file picker - for now using placeholder
                        adminViewModel.setSelectedThumbnailFile("sample_thumbnail.jpg")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Select Image", color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Day Selection
        Text(
            text = "Assign to Day:",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..5).map { "Day$it" }) { day ->
                FilterChip(
                    onClick = { adminViewModel.setSelectedDay(day) },
                    label = { Text(day) },
                    selected = selectedDay == day
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Upload Progress
        if (uploadProgress > 0f) {
            LinearProgressIndicator(
                progress = uploadProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color.Blue
            )
        }
        
        Button(
            onClick = {
                adminViewModel.uploadVideoWithFiles()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            enabled = uploadTitle.isNotEmpty() && 
                     adminViewModel.uploadDuration.collectAsState().value > 0 &&
                     selectedVideoFile != null && 
                     selectedThumbnailFile != null && 
                     !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Upload Video", color = Color.White)
            }
        }
    }
}

@Composable
private fun AdminAnalyticsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
        ) {
            Text(
                text = "Analytics dashboard will be implemented later",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun StudentCard(student: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = student.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = student.email,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = "${student.grade} - ${student.section}",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = student.schoolName,
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SessionCard(
    dayId: String,
    session: Session?,
    onToggleEnabled: (Boolean) -> Unit,
    onSessionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSessionClick() },
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = session?.title ?: dayId,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${session?.videos?.size ?: 0} videos • ${session?.permittedStudents?.size ?: 0} students",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (session?.enabled == true) {
                        Text(
                            text = "Tap to manage students",
                            color = Color.Blue.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Switch(
                    checked = session?.enabled ?: false,
                    onCheckedChange = onToggleEnabled
                )
            }
        }
    }
}

private data class AdminTab(
    val title: String,
    val icon: ImageVector
)
