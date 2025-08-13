package com.rotidote.app.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
    object VideoPlayer : Screen("video_player/{videoId}") {
        val arguments = listOf(
            navArgument("videoId") {
                type = NavType.StringType
            }
        )
        
        fun createRoute(videoId: String) = "video_player/$videoId"
    }
    object Upload : Screen("upload")
} 