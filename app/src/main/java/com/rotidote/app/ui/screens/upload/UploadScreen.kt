package com.rotidote.app.ui.screens.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rotidote.app.R
import com.rotidote.app.ui.components.LoadingSpinner
import com.rotidote.app.ui.viewmodels.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            onUploadSuccess()
        }
    }
    
    if (isUploading) {
        LoadingSpinner()
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.upload_video)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current

        UploadContent(
            onUpload = { creatorName, videoTitle, duration, adVideoUri, mainVideoUri, thumbnailUri ->
                viewModel.uploadVideo(
                    context = context,
                    creatorName = creatorName,
                    videoTitle = videoTitle,
                    duration = duration,
                    adVideoUri = adVideoUri,
                    mainVideoUri = mainVideoUri,
                    thumbnailUri = thumbnailUri
                )
            },
            errorMessage = error,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadContent(
    onUpload: (String, String, Long, Uri, Uri, Uri) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var creatorName by remember { mutableStateOf("") }
    var videoTitle by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var adVideoUri by remember { mutableStateOf<Uri?>(null) }
    var mainVideoUri by remember { mutableStateOf<Uri?>(null) }
    var thumbnailUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    
    // File picker launchers
    val adVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        adVideoUri = uri
    }
    
    val mainVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        mainVideoUri = uri
    }
    
    val thumbnailPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        thumbnailUri = uri
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.upload_video),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = creatorName,
            onValueChange = { creatorName = it },
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        OutlinedTextField(
            value = videoTitle,
            onValueChange = { videoTitle = it },
            label = { Text(stringResource(R.string.video_title)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text(stringResource(R.string.video_duration)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        // Ad Video Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (adVideoUri != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ad_video),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (adVideoUri != null) "Video selected" else "Select ad video",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { adVideoPicker.launch("video/*") }) {
                    Icon(Icons.Default.Add, contentDescription = "Select ad video")
                }
            }
        }
        
        // Main Video Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (mainVideoUri != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.main_video),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (mainVideoUri != null) "Video selected" else "Select main video",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { mainVideoPicker.launch("video/*") }) {
                    Icon(Icons.Default.Add, contentDescription = "Select main video")
                }
            }
        }
        
        // Thumbnail Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (thumbnailUri != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.thumbnail),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (thumbnailUri != null) "Image selected" else "Select thumbnail image",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { thumbnailPicker.launch("image/*") }) {
                    Icon(Icons.Default.Add, contentDescription = "Select thumbnail")
                }
            }
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = {
                val durationMs = duration.toLongOrNull() ?: 0L
                if (adVideoUri != null && mainVideoUri != null && thumbnailUri != null) {
                    onUpload(creatorName, videoTitle, durationMs, adVideoUri!!, mainVideoUri!!, thumbnailUri!!)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = creatorName.isNotEmpty() && 
                     videoTitle.isNotEmpty() && 
                     duration.isNotEmpty() && 
                     adVideoUri != null && 
                     mainVideoUri != null && 
                     thumbnailUri != null
        ) {
            Text(stringResource(R.string.upload_video))
        }
    }
} 