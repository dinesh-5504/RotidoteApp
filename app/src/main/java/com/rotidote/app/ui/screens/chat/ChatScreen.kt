package com.rotidote.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rotidote.app.R
import com.rotidote.app.ui.components.RotidoteLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("All") }
    
    Scaffold(
        modifier = Modifier.background(Color.Black),
        topBar = {
            TopAppBar(
                modifier = Modifier.background(Color.Black),
                title = { RotidoteLogo() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(
                            Icons.Default.Notifications, 
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Menu */ }) {
                        Icon(
                            Icons.Default.Menu, 
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.background(Color.Black),
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                NavigationBar(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToHome,
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                        label = { Text(stringResource(R.string.home)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToLeaderboard,
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = stringResource(R.string.leaderboard)) },
                        label = { Text(stringResource(R.string.leaderboard)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.chat)) },
                        label = { Text(stringResource(R.string.chat)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToProfile,
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
                        label = { Text(stringResource(R.string.profile)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            // Navigation Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("All", "Unread", "Community", "Friends")
                items(tabs) { tab ->
                    TabChip(
                        text = tab,
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }
            
            // Chat List
            LazyColumn(
                contentPadding = PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(getSampleChats()) { chat ->
                    ChatItem(
                        chat = chat,
                        onClick = { onNavigateToChat(chat.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) Color.Gray.copy(alpha = 0.3f) else Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ChatItem(
    chat: ChatItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = chat.avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Online indicator
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF31CC11))
                        .align(Alignment.BottomEnd)
                )
            }
        }
        
        // Chat Info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            // Name and pin
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chat.isOnline) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        tint = Color(0xFF31CC11),
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (chat.isPinned) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(12.dp)
                    )
                }
                
                if (chat.emoji != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = chat.emoji,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Last message
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Time and status
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = chat.lastMessageTime,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            when {
                chat.unreadCount > 0 -> {
                    // Unread count badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
                chat.isRead -> {
                    // Double checkmark for read
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "Read",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                }
                else -> {
                    // Single checkmark for sent
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = "Sent",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class ChatItem(
    val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val avatarUrl: String,
    val isOnline: Boolean = false,
    val isPinned: Boolean = false,
    val isRead: Boolean = false,
    val unreadCount: Int = 0,
    val emoji: String? = null
)

private fun getSampleChats(): List<ChatItem> {
    return listOf(
        ChatItem(
            id = "1",
            name = "Myself",
            lastMessage = "Today i performed well in the group discussion. i am very happyyy😊",
            lastMessageTime = "11:11 am",
            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop&crop=face",
            isPinned = true,
            isRead = true
        ),
        ChatItem(
            id = "2",
            name = "The OG. Group",
            lastMessage = "Eric: Guys, watch this bindblowing video. lierally this one break the myth.",
            lastMessageTime = "1:00 pm",
            avatarUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=100&h=100&fit=crop",
            isPinned = true,
            unreadCount = 2
        ),
        ChatItem(
            id = "3",
            name = "Fam Jam",
            lastMessage = "Dad: have safe journey anand 🫂",
            lastMessageTime = "25/05/25",
            avatarUrl = "https://images.unsplash.com/photo-1511895426328-dc8714191300?w=100&h=100&fit=crop"
        ),
        ChatItem(
            id = "4",
            name = "Tony stark",
            lastMessage = "you:watch this video on \"nano technology\" u might like this.",
            lastMessageTime = "24/05/25",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
            isOnline = true,
            isRead = true
        ),
        ChatItem(
            id = "5",
            name = "Parents & Teachers",
            lastMessage = "malini ma'am: tomorrow there will be a online meeting at 7 PM.",
            lastMessageTime = "25/05/2025",
            avatarUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=100&h=100&fit=crop"
        ),
        ChatItem(
            id = "6",
            name = "Mom",
            lastMessage = "The book is in the drawer under the desk.",
            lastMessageTime = "25/05/2025",
            avatarUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=100&h=100&fit=crop",
            emoji = "❤️",
            isOnline = true,
            unreadCount = 4
        ),
        ChatItem(
            id = "7",
            name = "chris",
            lastMessage = "Watch the video again to understand the topic better.",
            lastMessageTime = "25/05/2025",
            avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop"
        )
    )
}
