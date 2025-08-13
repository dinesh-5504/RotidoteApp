package com.rotidote.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rotidote.app.R
import com.rotidote.app.ui.components.LoadingSpinner
import com.rotidote.app.ui.components.RotidoteLogo
import com.rotidote.app.ui.viewmodels.AuthViewModel

@Composable
fun ProfileSetupScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        when (authState) {
            is com.rotidote.app.data.models.AuthState.Authenticated -> {
                // Navigate to home and clear the entire back stack
                onNavigateToHome()
            }
            else -> {}
        }
    }
    
    if (authState is com.rotidote.app.data.models.AuthState.Loading) {
        LoadingSpinner()
        return
    }
    
    ProfileSetupContent(
        onSaveProfile = { name, grade, section, schoolName ->
            viewModel.saveUserProfile(name, grade, section, schoolName)
        },
        errorMessage = if (authState is com.rotidote.app.data.models.AuthState.Error) {
            (authState as com.rotidote.app.data.models.AuthState.Error).message
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSetupContent(
    onSaveProfile: (String, String, String, String) -> Unit,
    errorMessage: String?
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        RotidoteLogo()
        
        Text(
            text = stringResource(R.string.profile_setup_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = stringResource(R.string.profile_setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.profile_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = grade,
            onValueChange = { grade = it },
            label = { Text(stringResource(R.string.profile_grade)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = section,
            onValueChange = { section = it },
            label = { Text(stringResource(R.string.profile_section)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = schoolName,
            onValueChange = { schoolName = it },
            label = { Text(stringResource(R.string.profile_school)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { onSaveProfile(name, grade, section, schoolName) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && grade.isNotBlank() && section.isNotBlank() && schoolName.isNotBlank()
        ) {
            Text(stringResource(R.string.profile_save))
        }
    }
}

