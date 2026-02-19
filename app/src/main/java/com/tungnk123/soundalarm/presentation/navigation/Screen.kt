package com.tungnk123.soundalarm.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AlarmDetail : Screen("alarm/{alarmId}") {
        fun createRoute(alarmId: Long) = "alarm/$alarmId"
    }
    data object Settings : Screen("settings")
    data object MusicSelection : Screen("music_selection")
    data object Playlist : Screen("playlist")
}
