package com.rotidote.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rotidote.app.R
import com.rotidote.app.data.models.Video
import com.rotidote.app.ui.components.*
import com.rotidote.app.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToVideoPlayer: (String, Boolean) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val videos by viewModel.videos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val error by viewModel.error.collectAsState()
    
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
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = stringResource(R.string.search),
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
                        selected = true,
                        onClick = { },
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
                        selected = false,
                        onClick = onNavigateToChat,
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
            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("For You", "Exposure", "Literacy")
                items(filters) { filter ->
                    FilterChip(
                        text = filter,
                        selected = selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) }
                    )
                }
            }
            
            // Video feed
            if (isLoading) {
                LoadingSpinner()
            } else if (error != null) {
                ErrorMessage(
                    message = error!!,
                    onRetry = { viewModel.refreshVideos() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Group videos by orientation
                    val landscapeVideos = videos.filter { it.orientation == "landscape" }
                    val portraitVideos = videos.filter { it.orientation == "portrait" }
                    
                    // Follow the specific order: 2L, 3P, 1L, 3P, 1L
                    var landscapeIndex = 0
                    var portraitIndex = 0
                    
                    // 1. 2 landscape videos
                    if (landscapeIndex < landscapeVideos.size) {
                        val endIndex = minOf(landscapeIndex + 2, landscapeVideos.size)
                        val landscapeGroup = landscapeVideos.subList(landscapeIndex, endIndex)
                        
                        items(landscapeGroup) { video ->
                            VerticalVideoCard(
                                video = video,
                                onClick = { 
                                    onNavigateToVideoPlayer(video.videoId, false) // Play ad for landscape videos
                                }
                            )
                        }
                        landscapeIndex = endIndex
                    }
                    
                    // 2. 3 portrait videos
                    if (portraitIndex < portraitVideos.size) {
                        val endIndex = minOf(portraitIndex + 3, portraitVideos.size)
                        val portraitGroup = portraitVideos.subList(portraitIndex, endIndex)
                        
                        item {
                            HorizontalVideoSection(
                                videos = portraitGroup,
                                onVideoClick = { videoId ->
                                    onNavigateToVideoPlayer(videoId, true) // Play main video directly for portrait videos
                                }
                            )
                        }
                        portraitIndex = endIndex
                    }
                    
                    // 3. 1 landscape video
                    if (landscapeIndex < landscapeVideos.size) {
                        val endIndex = minOf(landscapeIndex + 1, landscapeVideos.size)
                        val landscapeGroup = landscapeVideos.subList(landscapeIndex, endIndex)
                        
                        items(landscapeGroup) { video ->
                            VerticalVideoCard(
                                video = video,
                                onClick = { 
                                    onNavigateToVideoPlayer(video.videoId, false) // Play ad for landscape videos
                                }
                            )
                        }
                        landscapeIndex = endIndex
                    }
                    
                    // 4. 3 portrait videos
                    if (portraitIndex < portraitVideos.size) {
                        val endIndex = minOf(portraitIndex + 3, portraitVideos.size)
                        val portraitGroup = portraitVideos.subList(portraitIndex, endIndex)
                        
                        item {
                            HorizontalVideoSection(
                                videos = portraitGroup,
                                onVideoClick = { videoId ->
                                    onNavigateToVideoPlayer(videoId, true) // Play main video directly for portrait videos
                                }
                            )
                        }
                        portraitIndex = endIndex
                    }
                    
                    // 5. 1 landscape video
                    if (landscapeIndex < landscapeVideos.size) {
                        val endIndex = minOf(landscapeIndex + 1, landscapeVideos.size)
                        val landscapeGroup = landscapeVideos.subList(landscapeIndex, endIndex)
                        
                        items(landscapeGroup) { video ->
                            VerticalVideoCard(
                                video = video,
                                onClick = { 
                                    onNavigateToVideoPlayer(video.videoId, false) // Play ad for landscape videos
                                }
                            )
                        }
                        landscapeIndex = endIndex
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalVideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Thumbnail with duration overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = video.mainVideo.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Duration overlay (YouTube style)
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .background(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        
        // Video title below thumbnail (YouTube style)
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun HorizontalVideoSection(
    videos: List<Video>,
    onVideoClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        // Section title with genre
        val mainGenre = videos.firstOrNull()?.mainGenre ?: ""
        Text(
            text = "Explore $mainGenre",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Horizontal scrollable videos
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos) { video ->
                HorizontalVideoCard(
                    video = video,
                    onClick = { onVideoClick(video.videoId) }
                )
            }
        }
    }
}

@Composable
private fun HorizontalVideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(280.dp)
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(12.dp))
    ) {
        // Thumbnail only (no title, profile, creator name, or 3-dot menu)
        AsyncImage(
            model = video.mainVideo.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Duration overlay (YouTube style)
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd)
                .background(
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = video.duration,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
} 