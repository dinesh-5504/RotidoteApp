package com.rotidote.app.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
    object DaySessionsHome : Screen("day_sessions_home")
    object VideoPlayer : Screen("video_player/{videoId}?skipAd={skipAd}") {
        val arguments = listOf(
            navArgument("videoId") {
                type = NavType.StringType
            },
            navArgument("skipAd") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
        
        fun createRoute(videoId: String, skipAd: Boolean = false) = "video_player/$videoId?skipAd=$skipAd"
    }
    object Upload : Screen("upload")
    object Leaderboard : Screen("leaderboard")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object AdminDashboard : Screen("admin_dashboard?tab={tab}") {
        val arguments = listOf(
            navArgument("tab") {
                type = NavType.StringType
                defaultValue = "students"
            }
        )
        
        fun createRoute(tab: String = "students") = "admin_dashboard?tab=$tab"
    }
    object AdminStudents : Screen("admin_students")
    object AdminSessions : Screen("admin_sessions")
    object AdminSessionDetail : Screen("admin_session_detail/{dayId}") {
        val arguments = listOf(
            navArgument("dayId") {
                type = NavType.StringType
            }
        )
        
        fun createRoute(dayId: String) = "admin_session_detail/$dayId"
    }
    object AdminUpload : Screen("admin_upload")
    object AdminAnalytics : Screen("admin_analytics")
    object PersonalChat : Screen("personal_chat/{contactId}") {
        val arguments = listOf(
            navArgument("contactId") {
                type = NavType.StringType
            }
        )
        
        fun createRoute(contactId: String) = "personal_chat/$contactId"
    }
} 