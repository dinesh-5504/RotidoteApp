package com.rotidote.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rotidote.app.R

@Composable
fun LoadingSpinner() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}



@Composable
fun RotidoteLogo(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.FilterChip(
        onClick = onClick,
        label = { Text(text = text) },
        selected = selected,
        modifier = modifier
    )
}

@Composable
fun VideoThumbnail(
    thumbnailUrl: String,
    duration: Long,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // In a real app, you would use Coil to load the image
        // AsyncImage(
        //     model = thumbnailUrl,
        //     contentDescription = null,
        //     modifier = Modifier.fillMaxSize(),
        //     contentScale = ContentScale.Crop
        // )
        
        // Placeholder for now
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Thumbnail",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Duration overlay
        Surface(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
} 