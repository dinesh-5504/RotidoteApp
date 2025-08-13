package com.rotidote.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rotidote.app.R
import com.rotidote.app.data.models.Video
import com.rotidote.app.ui.components.*
import com.rotidote.app.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToVideoPlayer: (String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToUpload: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val videos by viewModel.videos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val error by viewModel.error.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { RotidoteLogo() },
                actions = {
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.notifications))
                    }
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                    IconButton(onClick = onNavigateToUpload) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.upload_video))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home)) },
                        label = { Text(stringResource(R.string.home)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToLeaderboard,
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = stringResource(R.string.leaderboard)) },
                        label = { Text(stringResource(R.string.leaderboard)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToChat,
                        icon = { Icon(Icons.Default.Chat, contentDescription = stringResource(R.string.chat)) },
                        label = { Text(stringResource(R.string.chat)) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToProfile,
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile)) },
                        label = { Text(stringResource(R.string.profile)) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(videos) { video ->
                        VideoCard(
                            video = video,
                            onClick = { onNavigateToVideoPlayer(video.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            VideoThumbnail(
                thumbnailUrl = video.thumbnailUrl,
                duration = video.duration,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = video.creatorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = video.likes.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Comment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = video.comments.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
} 