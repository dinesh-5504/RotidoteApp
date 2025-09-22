package com.rotidote.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.rotidote.app.data.models.AuthState
import com.rotidote.app.data.models.UserType
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
                    val shouldFetchAdminToken by authViewModel.shouldFetchAdminToken.collectAsState()
                    
                    // Listen for admin token fetch signal
                    LaunchedEffect(shouldFetchAdminToken) {
                        Log.d("MainActivity", "🔍 LaunchedEffect triggered - shouldFetchAdminToken: $shouldFetchAdminToken")
                        if (shouldFetchAdminToken) {
                            Log.d("MainActivity", "🚀 Admin token fetch triggered")
                            
                            // Add a small delay to ensure Firebase Auth is fully completed
                            kotlinx.coroutines.delay(1000)
                            Log.d("MainActivity", "⏰ Delay completed, calling fetchAdminToken")
                            
                            authViewModel.fetchAdminToken()
                            Log.d("MainActivity", "🔄 Resetting admin token fetch flag")
                            authViewModel.resetAdminTokenFetchFlag()
                        }
                    }
                    
                    // Handle navigation based on auth state changes
                    LaunchedEffect(authState) {
                        Log.d("MainActivity", "🔄 Auth state changed: $authState")
                        when (val state = authState) {
                            is AuthState.Authenticated -> {
                                Log.d("MainActivity", "✅ User authenticated with type: ${state.userType}")
                                when (state.userType) {
                                    UserType.ADMIN -> {
                                        Log.d("MainActivity", "🔐 Admin authenticated - fetching token before navigation")
                                        // Fetch admin token immediately for first login
                                        authViewModel.fetchAdminToken()
                                        
                                        Log.d("MainActivity", "🚀 Navigating to AdminDashboard")
                                        navController.navigate(Screen.AdminDashboard.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                    UserType.STUDENT -> {
                                        Log.d("MainActivity", "🎓 Student authenticated - navigating to Home")
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            is AuthState.ProfileIncomplete -> {
                                Log.d("MainActivity", "📝 Profile incomplete - navigating to ProfileSetup")
                                navController.navigate(Screen.ProfileSetup.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            is AuthState.DaySessionsAvailable -> {
                                Log.d("MainActivity", "📅 Day sessions available - navigating to DaySessionsHome")
                                navController.navigate(Screen.DaySessionsHome.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                            is AuthState.Loading -> {
                                Log.d("MainActivity", "⏳ Auth state is loading - staying on current screen")
                            }
                            is AuthState.Unauthenticated -> {
                                Log.d("MainActivity", "❌ User unauthenticated - staying on Login screen")
                            }
                            is AuthState.Error -> {
                                Log.d("MainActivity", "❌ Auth error: ${state.message} - staying on Login screen")
                            }
                        }
                    }
                    
                    NavGraph(
                        navController = navController,
                        startDestination = Screen.Login.route
                    )
                }
            }
        }
    }
}