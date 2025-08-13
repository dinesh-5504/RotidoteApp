package com.rotidote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rotidote.app.ui.screens.auth.LoginScreen
import com.rotidote.app.ui.screens.auth.ProfileSetupScreen
import com.rotidote.app.ui.screens.auth.SignupScreen
import com.rotidote.app.ui.screens.home.HomeScreen
import com.rotidote.app.ui.screens.upload.UploadScreen
import com.rotidote.app.ui.screens.video.VideoPlayerScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        // Clear the entire back stack so user can't go back to login/profile setup
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToVideoPlayer = { videoId ->
                    navController.navigate(Screen.VideoPlayer.createRoute(videoId))
                },
                onNavigateToLeaderboard = {
                    // TODO: Implement leaderboard screen
                },
                onNavigateToChat = {
                    // TODO: Implement chat screen
                },
                onNavigateToProfile = {
                    // TODO: Implement profile screen
                },
                onNavigateToUpload = {
                    navController.navigate(Screen.Upload.route)
                }
            )
        }
        
        composable(
            route = Screen.VideoPlayer.route,
            arguments = Screen.VideoPlayer.arguments
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            VideoPlayerScreen(
                videoId = videoId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Upload.route) {
            UploadScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUploadSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
} 