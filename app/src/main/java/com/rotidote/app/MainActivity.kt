package com.rotidote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.rotidote.app.data.models.AuthState
import com.rotidote.app.ui.navigation.NavGraph
import com.rotidote.app.ui.navigation.Screen
import com.rotidote.app.ui.theme.RotidoteTheme
import com.rotidote.app.ui.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RotidoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState by authViewModel.authState.collectAsState()
                    
                    val startDestination = when (authState) {
                        is AuthState.Authenticated -> Screen.Home.route
                        is AuthState.ProfileIncomplete -> Screen.ProfileSetup.route
                        else -> Screen.Login.route
                    }
                    
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
} 