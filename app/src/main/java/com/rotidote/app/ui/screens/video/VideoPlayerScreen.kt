package com.rotidote.app.ui.screens.video

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.rotidote.app.R
import com.rotidote.app.data.models.Video
import com.rotidote.app.ui.components.LoadingSpinner
import com.rotidote.app.ui.components.ErrorMessage
import com.rotidote.app.ui.components.RotidoteLogo
import com.rotidote.app.ui.components.FilterChip
import com.rotidote.app.ui.viewmodels.VideoPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    skipAd: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val video by viewModel.video.collectAsState()
    val adVideoUrl by viewModel.adVideoUrl.collectAsState()
    val mainVideoUrl by viewModel.mainVideoUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    // These must be provided by your ViewModel (if not, remove/change)
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val videos by viewModel.videos.collectAsState()

    var showComments by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf(TextFieldValue("")) }
    var selectedCommentFilter by remember { mutableStateOf("For You") }

    LaunchedEffect(videoId) {
        viewModel.loadVideo(videoId, skipAd)
    }

    if (isLoading) {
        LoadingSpinner()
        return
    }

    if (error != null) {
        ErrorMessage(
            message = error!!,
            onRetry = { viewModel.loadVideo(videoId, skipAd) }
        )
        return
    }

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
                title = { RotidoteLogo() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Video Player area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    if (video != null && mainVideoUrl != null) {
                        val shouldPlayAd = (video!!.orientation == "landscape" && !skipAd)

                        if (shouldPlayAd && adVideoUrl != null) {
                            ChainedVideoPlayer(
                                adVideoUrl = adVideoUrl!!,
                                mainVideoUrl = mainVideoUrl!!,
                                onAdFinished = { viewModel.onAdFinished() },
                                onMainVideoFinished = { viewModel.onMainVideoFinished() },
                                isPlaying = isPlaying,
                                onPlayPause = { viewModel.togglePlayPause() },
                                onSeek = { position -> viewModel.seekTo(position) },
                                currentPosition = currentPosition,
                                duration = duration,
                                isAdVideo = true
                            )
                        } else {
                            MainVideoPlayer(
                                videoUrl = mainVideoUrl!!,
                                onVideoFinished = { viewModel.onMainVideoFinished() },
                                isPlaying = isPlaying,
                                onPlayPause = { viewModel.togglePlayPause() },
                                onSeek = { position -> viewModel.seekTo(position) },
                                onSkipForward = { viewModel.skipForward() },
                                onSkipBackward = { viewModel.skipBackward() },
                                currentPosition = currentPosition,
                                duration = duration,
                                isAdVideo = false
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }

                // Content below the player (title, creator, actions, comments, related)
                video?.let { videoData ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Title
                        item {
                            Text(
                                text = videoData.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        // Creator row
                        item {
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    color = Color.Gray.copy(alpha = 0.3f)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }

                                Text(
                                    text = videoData.creatorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }

                                                 // Reaction / Action row (LIKE, DISLIKE, SHARE, DOWNLOAD, SAVE)
                         item {
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth()
                                     .padding(vertical = 16.dp),
                                 horizontalArrangement = Arrangement.SpaceEvenly,
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 var isLiked by remember { mutableStateOf(false) }
                                 var isDisliked by remember { mutableStateOf(false) }
                                 var isSaved by remember { mutableStateOf(false) }
                                 var isShared by remember { mutableStateOf(false) }
                                 var isDownloaded by remember { mutableStateOf(false) }

                                 // 👍 Like
                                 LikeDislikeIcon(Icons.Filled.ThumbUp, isLiked) {
                                     isLiked = !isLiked
                                     if (isLiked) isDisliked = false
                                 }

                                 // 👎 Dislike
                                 LikeDislikeIcon(Icons.Filled.ThumbDown, isDisliked) {
                                     isDisliked = !isDisliked
                                     if (isDisliked) isLiked = false
                                 }

                                 // 📤 Share
                                 SimpleIcon(Icons.Default.Share) {
                                     isShared = !isShared
                                 }

                                 // ⬇ Download
                                 SimpleIcon(Icons.Default.FileDownload) {
                                     isDownloaded = !isDownloaded
                                 }

                                 // 💾 Save
                                 SimpleIcon(Icons.Default.Save) {
                                     isSaved = !isSaved
                                 }
                             }
                         }


                        // Comments preview + input
                        item {
                            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                                Text(
                                    text = stringResource(R.string.comments),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .clickable { showComments = !showComments }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        color = Color.Gray.copy(alpha = 0.3f)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        placeholder = { Text(stringResource(R.string.add_comment), color = Color.Gray) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.Gray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                        }

                        // Deepen box (centered, gradient)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                DeepenBox(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    onClick = {
                                        // TODO: navigate to AI feature
                                    }
                                )
                            }
                        }

                        // Related videos (landscape) - reuse VerticalVideoCard
                        val relatedVideos = videos.filter { it.orientation == "landscape" }.take(3)
                        if (relatedVideos.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.related_videos),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }

                            items(relatedVideos) { relatedVideo ->
                                VerticalVideoCard(
                                    video = relatedVideo,
                                    onClick = {
                                        // navigate or open the related video
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Comments overlay sheet
            if (showComments) {
                CommentsOverlay(
                    onClose = { showComments = false },
                    commentText = commentText,
                    onCommentTextChange = { commentText = it },
                    selectedFilter = selectedCommentFilter,
                    onFilterChange = { selectedCommentFilter = it }
                )
            }
        }
    }
}

/* ---------------------
   Reaction icon helpers
   --------------------- */

@Composable
private fun LikeDislikeIcon(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SimpleIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/* ---------------------
   Deepen box (gradient)
   --------------------- */

@Composable
private fun DeepenBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFD04040), Color(0xFF31CC11))
                    )
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Deepen your thoughts with AI",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                color = Color.White
            )
        }
    }
}

/* ---------------------
   Player composables (unchanged)
   --------------------- */

@Composable
private fun ChainedVideoPlayer(
    adVideoUrl: String,
    mainVideoUrl: String,
    onAdFinished: () -> Unit,
    onMainVideoFinished: () -> Unit,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    currentPosition: Long,
    duration: Long,
    isAdVideo: Boolean
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    if (adVideoUrl.isEmpty() || mainVideoUrl.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Invalid video URLs")
        }
        return
    }

    DisposableEffect(context) {
        val player = ExoPlayer.Builder(context).build()
        exoPlayer = player

        val adMediaItem = MediaItem.fromUri(adVideoUrl)
        val mainMediaItem = MediaItem.fromUri(mainVideoUrl)

        player.setMediaItems(listOf(adMediaItem, mainMediaItem))
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    if (player.currentMediaItemIndex == 0) {
                        onAdFinished()
                    } else {
                        onMainVideoFinished()
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("VideoPlayer", "ExoPlayer error: ${error.message}", error)
            }
        })

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { playerView -> playerView.player = exoPlayer }
        )
    }
}

@Composable
private fun MainVideoPlayer(
    videoUrl: String,
    onVideoFinished: () -> Unit,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    currentPosition: Long,
    duration: Long,
    isAdVideo: Boolean
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    if (videoUrl.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Invalid video URL")
        }
        return
    }

    DisposableEffect(context) {
        val player = ExoPlayer.Builder(context).build()
        exoPlayer = player

        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onVideoFinished()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("VideoPlayer", "ExoPlayer error: ${error.message}", error)
            }
        })

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { playerView -> playerView.player = exoPlayer }
        )
    }
}

/* ---------------------
   CommentsOverlay & VerticalVideoCard (unchanged logic)
   --------------------- */

@Composable
private fun CommentsOverlay(
    onClose: () -> Unit,
    commentText: TextFieldValue,
    onCommentTextChange: (TextFieldValue) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.comments),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("For You", "Top")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { onFilterChange(filter) },
                            label = { Text(filter) }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_comments),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            color = Color.Gray.copy(alpha = 0.3f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = onCommentTextChange,
                            placeholder = {
                                Text(
                                    stringResource(R.string.add_comment),
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
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
