package com.rotidote.app.ui.screens.video

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.rotidote.app.R
import com.rotidote.app.ui.components.LoadingSpinner
import com.rotidote.app.ui.components.ErrorMessage
import com.rotidote.app.ui.viewmodels.VideoPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    onNavigateBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val video by viewModel.video.collectAsState()
    val adVideoUrl by viewModel.adVideoUrl.collectAsState()
    val mainVideoUrl by viewModel.mainVideoUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAdPlaying by viewModel.isAdPlaying.collectAsState()
    
    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId)
    }
    
    if (isLoading) {
        LoadingSpinner()
        return
    }
    
    if (error != null) {
        ErrorMessage(
            message = error!!,
            onRetry = { viewModel.loadVideo(videoId) }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(video?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Video Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                if (isAdPlaying && adVideoUrl != null) {
                    AdVideoPlayer(
                        videoUrl = adVideoUrl!!,
                        onAdFinished = { viewModel.onAdFinished() }
                    )
                } else if (!isAdPlaying && mainVideoUrl != null) {
                    MainVideoPlayer(
                        videoUrl = mainVideoUrl!!,
                        onVideoFinished = { viewModel.onMainVideoFinished() }
                    )
                }
            }
            
            // Video Info
            video?.let { videoData ->
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = videoData.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = videoData.creatorName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.likeVideo() }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = stringResource(R.string.like))
                                Text(
                                    text = videoData.likes.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { viewModel.dislikeVideo() }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ThumbDown, contentDescription = stringResource(R.string.dislike))
                                Text(
                                    text = videoData.dislikes.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { /* TODO: Comments */ }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Comment, contentDescription = stringResource(R.string.comment))
                                Text(
                                    text = videoData.comments.toString(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdVideoPlayer(
    videoUrl: String,
    onAdFinished: () -> Unit
) {
    val context = LocalContext.current
    
    DisposableEffect(context) {
        val exoPlayer = ExoPlayer.Builder(context).build()
        val playerView = PlayerView(context).apply {
            player = exoPlayer
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onAdFinished()
                }
            }
        })
        
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun MainVideoPlayer(
    videoUrl: String,
    onVideoFinished: () -> Unit
) {
    val context = LocalContext.current
    
    DisposableEffect(context) {
        val exoPlayer = ExoPlayer.Builder(context).build()
        val playerView = PlayerView(context).apply {
            player = exoPlayer
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        
        val mediaItem = MediaItem.fromUri(videoUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoFinished()
                }
            }
        })
        
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
} 