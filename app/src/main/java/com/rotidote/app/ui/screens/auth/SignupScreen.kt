package com.rotidote.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rotidote.app.R
import com.rotidote.app.ui.components.LoadingSpinner
import com.rotidote.app.ui.components.RotidoteLogo
import com.rotidote.app.ui.viewmodels.AuthViewModel

@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    
    LaunchedEffect(authState) {
        when (authState) {
            is com.rotidote.app.data.models.AuthState.ProfileIncomplete -> {
                onNavigateToProfile()
            }
            else -> {}
        }
    }
    
    if (authState is com.rotidote.app.data.models.AuthState.Loading) {
        LoadingSpinner()
        return
    }
    
    SignupContent(
        onSignup = { email, password, confirmPassword ->
            if (password == confirmPassword) {
                viewModel.signUp(email, password)
            }
        },
        onNavigateToLogin = onNavigateToLogin,
        errorMessage = if (authState is com.rotidote.app.data.models.AuthState.Error) {
            (authState as com.rotidote.app.data.models.AuthState.Error).message
        } else null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignupContent(
    onSignup: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    errorMessage: String?
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RotidoteLogo(
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                passwordError = null
            },
            label = { Text(stringResource(R.string.password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = passwordError != null
        )
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                passwordError = null
            },
            label = { Text(stringResource(R.string.confirm_password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true,
            isError = passwordError != null
        )
        
        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
                if (password != confirmPassword) {
                    passwordError = "Passwords do not match"
                    return@Button
                }
                if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters"
                    return@Button
                }
                isLoading = true
                onSignup(email, password, confirmPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.signup))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onNavigateToLogin
        ) {
            Text(stringResource(R.string.already_have_account))
        }
    }
} 