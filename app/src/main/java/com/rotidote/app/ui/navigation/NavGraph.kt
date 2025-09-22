package com.rotidote.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.rotidote.app.ui.screens.auth.LoginScreen
import com.rotidote.app.ui.screens.auth.ProfileSetupScreen
import com.rotidote.app.ui.screens.auth.SignupScreen
import com.rotidote.app.ui.screens.home.HomeScreen
import com.rotidote.app.ui.screens.student.DaySessionsHomeScreen
import com.rotidote.app.ui.screens.upload.UploadScreen
import com.rotidote.app.ui.screens.video.VideoPlayerScreen
import com.rotidote.app.ui.screens.chat.ChatScreen
import com.rotidote.app.ui.screens.chat.PersonalChatScreen
import com.rotidote.app.ui.screens.leaderboard.LeaderboardScreen
import com.rotidote.app.ui.screens.profile.ProfileScreen
import com.rotidote.app.ui.screens.admin.AdminDashboardScreen
import com.rotidote.app.ui.screens.admin.SessionDetailScreen

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
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
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
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        // Clear the entire back stack so user can't go back to login/profile setup
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToVideoPlayer = { videoId, skipAd ->
                    navController.navigate(Screen.VideoPlayer.createRoute(videoId, skipAd))
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.DaySessionsHome.route) {
            DaySessionsHomeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToVideoPlayer = { videoUrl ->
                    // Navigate to video player with Mux URL
                    navController.navigate(Screen.VideoPlayer.createRoute(videoUrl, false))
                }
            )
        }
        
        composable(
            route = Screen.VideoPlayer.route,
            arguments = Screen.VideoPlayer.arguments
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
            val skipAd = backStackEntry.arguments?.getBoolean("skipAd") ?: false
            VideoPlayerScreen(
                videoId = videoId,
                skipAd = skipAd,
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
        
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Leaderboard.route) { inclusive = true }
                    }
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Leaderboard.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.PersonalChat.createRoute(chatId))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                    }
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route) {
                        popUpTo(Screen.Chat.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(
            route = Screen.PersonalChat.route,
            arguments = Screen.PersonalChat.arguments
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            PersonalChatScreen(
                contactName = if (contactId == "4") "Tony Stark" else "Contact",
                contactAvatarUrl = if (contactId == "4") "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop" else "",
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = Screen.AdminDashboard.route,
            arguments = Screen.AdminDashboard.arguments
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "students"
            AdminDashboardScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSessionDetail = { session ->
                    navController.navigate(Screen.AdminSessionDetail.createRoute(session.dayId))
                },
                initialTab = tab
            )
        }
        
        composable(
            route = Screen.AdminSessionDetail.route,
            arguments = Screen.AdminSessionDetail.arguments
        ) { backStackEntry ->
            val dayId = backStackEntry.arguments?.getString("dayId") ?: ""
            val adminViewModel: com.rotidote.app.ui.viewmodels.AdminViewModel = hiltViewModel()
            val sessions by adminViewModel.sessions.collectAsState()
            
            // Find the session or create a default one
            val session = sessions.find { it.dayId == dayId } 
                ?: com.rotidote.app.data.models.Session(
                    dayId = dayId,
                    title = dayId,
                    enabled = true,
                    videos = emptyList(),
                    permittedStudents = emptyList()
                )
            
            SessionDetailScreen(
                session = session,
                onNavigateBack = {
                    // Navigate back to AdminDashboard with Sessions tab selected
                    navController.navigate(Screen.AdminDashboard.createRoute("sessions")) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = false }
                    }
                }
            )
        }
    }
} 