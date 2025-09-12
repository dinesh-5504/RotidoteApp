package com.rotidote.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rotidote.app.ui.components.RotidoteLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalChatScreen(
    contactName: String = "Tony Stark",
    contactAvatarUrl: String = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = Modifier.background(Color.Black),
        topBar = {
            TopAppBar(
                modifier = Modifier.background(Color.Black),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Contact Avatar
                        AsyncImage(
                            model = contactAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Contact Name
                        Text(
                            text = contactName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Video call button
                    IconButton(onClick = { /* TODO: Video call */ }) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = "Video call",
                            tint = Color.White
                        )
                    }
                    
                    // Voice call button
                    IconButton(onClick = { /* TODO: Voice call */ }) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Voice call",
                            tint = Color.White
                        )
                    }
                    
                    // More options
                    IconButton(onClick = { /* TODO: More options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Message Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Gray.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji button
                    IconButton(onClick = { /* TODO: Emoji picker */ }) {
                        Icon(
                            Icons.Default.SentimentSatisfied,
                            contentDescription = "Emoji",
                            tint = Color.White
                        )
                    }
                    
                    // Message input field
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = {
                            Text(
                                text = "Message here",
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    
                    // Attachment button
                    IconButton(onClick = { /* TODO: Attach file */ }) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attach file",
                            tint = Color.White
                        )
                    }
                    
                    // Image button
                    IconButton(onClick = { /* TODO: Attach image */ }) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Attach image",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        // Chat Messages Area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getSampleMessages()) { message ->
                MessageBubble(message = message)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    if (message.isFromMe) {
        // My message (right side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = Color(0xFF2196F3),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 4.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = message.time,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    } else {
        // Contact's message (left side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = message.time,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean
)

private fun getSampleMessages(): List<ChatMessage> {
    return listOf(
        ChatMessage(
            id = "1",
            text = "Hey! How's your day going?",
            time = "10:30 AM",
            isFromMe = false
        ),
        ChatMessage(
            id = "2",
            text = "Hi Tony! It's going great, thanks for asking. How about yours?",
            time = "10:32 AM",
            isFromMe = true
        ),
        ChatMessage(
            id = "3",
            text = "Pretty good! Just finished working on some new tech. You should check out this video on nanotechnology I found.",
            time = "10:35 AM",
            isFromMe = false
        ),
        ChatMessage(
            id = "4",
            text = "That sounds interesting! What's it about?",
            time = "10:37 AM",
            isFromMe = true
        ),
        ChatMessage(
            id = "5",
            text = "It's about how we can manipulate matter at the atomic level. Really groundbreaking stuff. I'll send you the link.",
            time = "10:40 AM",
            isFromMe = false
        ),
        ChatMessage(
            id = "6",
            text = "Thanks! I'll definitely watch it. Always love learning about new technology.",
            time = "10:42 AM",
            isFromMe = true
        ),
        ChatMessage(
            id = "7",
            text = "Great! Let me know what you think after watching it. We can discuss the implications.",
            time = "10:45 AM",
            isFromMe = false
        ),
        ChatMessage(
            id = "8",
            text = "Will do! By the way, how's the project you were working on last week?",
            time = "10:47 AM",
            isFromMe = true
        ),
        ChatMessage(
            id = "9",
            text = "Making good progress! Should be ready for testing soon. I'll keep you updated.",
            time = "10:50 AM",
            isFromMe = false
        ),
        ChatMessage(
            id = "10",
            text = "Perfect! Looking forward to seeing it in action.",
            time = "10:52 AM",
            isFromMe = true
        )
    )
}
