package com.rotidote.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rotidote.app.data.models.Session
import com.rotidote.app.data.models.User
import com.rotidote.app.ui.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: Session,
    onNavigateBack: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val sessionStudents by adminViewModel.sessionStudents.collectAsState()
    val allStudents by adminViewModel.students.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    var showAddStudentDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(session) {
        adminViewModel.selectSession(session)
    }
    
    Scaffold(
        modifier = Modifier.background(Color.Black),
        topBar = {
            TopAppBar(
                title = { Text("${session.title} - Students", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showAddStudentDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Session Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = session.title,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: ${if (session.enabled) "Enabled" else "Disabled"}",
                        color = if (session.enabled) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Videos: ${session.videos.size}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Permitted Students: ${sessionStudents.size}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Students List
            Text(
                text = "Permitted Students",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (sessionStudents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                ) {
                    Text(
                        text = "No students permitted for this session yet.",
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessionStudents) { student ->
                        SessionStudentCard(
                            student = student,
                            onRemove = {
                                adminViewModel.removeStudentFromSession(session.dayId, student.id)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add Student Dialog
    if (showAddStudentDialog) {
        AddStudentDialog(
            allStudents = allStudents,
            permittedStudents = sessionStudents,
            onAddStudent = { student ->
                adminViewModel.addStudentToSession(session.dayId, student.id)
                showAddStudentDialog = false
            },
            onDismiss = { showAddStudentDialog = false }
        )
    }
}

@Composable
private fun SessionStudentCard(
    student: User,
    onRemove: () -> Unit
) {
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
                    text = student.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = student.email,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${student.grade} - ${student.section} | ${student.schoolName}",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove Student",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
private fun AddStudentDialog(
    allStudents: List<User>,
    permittedStudents: List<User>,
    onAddStudent: (User) -> Unit,
    onDismiss: () -> Unit
) {
    val availableStudents = allStudents.filter { student ->
        !permittedStudents.any { it.id == student.id }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Student to Session", color = Color.White)
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(300.dp)
            ) {
                if (availableStudents.isEmpty()) {
                    item {
                        Text(
                            "All students are already permitted for this session.",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    items(availableStudents) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${student.grade} - ${student.section}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                IconButton(onClick = { onAddStudent(student) }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Add Student",
                                        tint = Color.Green
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        },
        containerColor = Color.Black,
        textContentColor = Color.White
    )
}

