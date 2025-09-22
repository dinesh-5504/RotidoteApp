package com.rotidote.app.ui.screens.upload

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        modifier = Modifier.background(Color.Black),
        topBar = {
            TopAppBar(
                modifier = Modifier.background(Color.Black),
                title = { Text(stringResource(R.string.upload_video), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        UploadContent(
            onUpload = { creatorName, videoTitle, duration, mainGenre, mainVideoPlaybackId, adGenre, adVideoPlaybackId, thumbnailUrl, orientation ->
                viewModel.uploadVideo(
                    creatorName = creatorName,
                    videoTitle = videoTitle,
                    duration = duration,
                    mainGenre = mainGenre,
                    mainVideoPlaybackId = mainVideoPlaybackId,
                    adGenre = adGenre,
                    adVideoPlaybackId = adVideoPlaybackId,
                    thumbnailUrl = thumbnailUrl,
                    orientation = orientation
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
    onUpload: (String, String, String, String, String, String, String, String, String) -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var creatorName by remember { mutableStateOf("") }
    var videoTitle by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var mainGenre by remember { mutableStateOf("") }
    var mainVideoPlaybackId by remember { mutableStateOf("") }
    var adGenre by remember { mutableStateOf("") }
    var adVideoPlaybackId by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var orientation by remember { mutableStateOf("landscape") }
    val scrollState = rememberScrollState()

         Column(
         modifier = modifier
             .fillMaxSize()
             .background(Color.Black)
             .padding(24.dp)
             .verticalScroll(scrollState), // 👈 make it scrollable
         horizontalAlignment = Alignment.CenterHorizontally,
         //verticalArrangement = Arrangement.SpaceBetween
     ) {
         Column(
             horizontalAlignment = Alignment.CenterHorizontally
         ) {

        
        OutlinedTextField(
            value = creatorName,
            onValueChange = { creatorName = it },
            label = { Text("Creator Name", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = videoTitle,
            onValueChange = { videoTitle = it },
            label = { Text("Video Title", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (mm:ss)", color = Color.White) },
            placeholder = { Text("05:32", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        // Orientation selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
                                 Text(
                         text = stringResource(R.string.video_orientation),
                         style = MaterialTheme.typography.bodyMedium,
                         color = Color.White,
                         modifier = Modifier.padding(bottom = 8.dp)
                     )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = orientation == "landscape",
                        onClick = { orientation = "landscape" }
                    )
                                         Text(
                         text = stringResource(R.string.orientation_landscape),
                         style = MaterialTheme.typography.bodyMedium,
                         color = Color.White,
                         modifier = Modifier.padding(start = 8.dp)
                     )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = orientation == "portrait",
                        onClick = { orientation = "portrait" }
                    )
                                         Text(
                         text = stringResource(R.string.orientation_portrait),
                         style = MaterialTheme.typography.bodyMedium,
                         color = Color.White,
                         modifier = Modifier.padding(start = 8.dp)
                     )
                }
            }
        }
        
        OutlinedTextField(
            value = mainGenre,
            onValueChange = { mainGenre = it },
            label = { Text("Main Video Genre", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = mainVideoPlaybackId,
            onValueChange = { mainVideoPlaybackId = it },
            label = { Text("Main Video Playback ID", color = Color.White) },
            placeholder = { Text("main12345", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = adGenre,
            onValueChange = { adGenre = it },
            label = { Text("Ad Video Genre", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = adVideoPlaybackId,
            onValueChange = { adVideoPlaybackId = it },
            label = { Text("Ad Video Playback ID", color = Color.White) },
            placeholder = { Text("ad67890", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
        OutlinedTextField(
            value = thumbnailUrl,
            onValueChange = { thumbnailUrl = it },
            label = { Text("Thumbnail URL (Cloudinary)", color = Color.White) },
            placeholder = { Text("https://cloudinary.com/...thumb.jpg", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray
            )
        )
        
                          if (errorMessage != null) {
             Text(
                 text = errorMessage,
                 color = Color.Red,
                 textAlign = TextAlign.Center,
                 modifier = Modifier.padding(bottom = 16.dp)
             )
         }
         }
         
         // Upload button at the bottom
         Button(
             onClick = {
                 onUpload(creatorName, videoTitle, duration, mainGenre, mainVideoPlaybackId, adGenre, adVideoPlaybackId, thumbnailUrl, orientation)
             },
             modifier = Modifier
                 .fillMaxWidth()
                 .height(56.dp),
             enabled = creatorName.isNotEmpty() && 
                      videoTitle.isNotEmpty() && 
                      duration.isNotEmpty() && 
                      mainGenre.isNotEmpty() && 
                      mainVideoPlaybackId.isNotEmpty() && 
                      adGenre.isNotEmpty() && 
                      adVideoPlaybackId.isNotEmpty() && 
                      thumbnailUrl.isNotEmpty() &&
                      orientation.isNotEmpty(),
             colors = ButtonDefaults.buttonColors(
                 containerColor = Color.White,
                 contentColor = Color.Black
             ),
             shape = RoundedCornerShape(12.dp)
         ) {
             Text(
                 text = "Upload Video",
                 style = MaterialTheme.typography.titleMedium,
                 fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
             )
         }
    }
} 