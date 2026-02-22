package com.tungnk123.soundalarm.presentation.navigation

sealed class Screen(val route: String) {
    // Bottom nav tabs
    data object Home : Screen("home")
    data object Sounds : Screen("sounds")
    data object Challenge : Screen("challenge")
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")

    // Sub-screens (push/pop)
    data object AlarmDetail : Screen("alarm/{alarmId}") {
        fun createRoute(alarmId: Long) = "alarm/$alarmId"
    }
    data object MusicSelection : Screen("music_selection/{playlistGroupId}") {
        fun createRoute(playlistGroupId: Long) = "music_selection/$playlistGroupId"
    }
    data object PlaylistGroups : Screen("playlist_groups")
    data object PlaylistDetail : Screen("playlist/{playlistGroupId}") {
        fun createRoute(playlistGroupId: Long) = "playlist/$playlistGroupId"
    }
}
