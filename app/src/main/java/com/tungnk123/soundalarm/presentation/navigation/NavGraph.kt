package com.tungnk123.soundalarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tungnk123.soundalarm.presentation.alarm.AlarmDetailScreen
import com.tungnk123.soundalarm.presentation.home.HomeScreen
import com.tungnk123.soundalarm.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAlarmDetail = { alarmId ->
                    navController.navigate(Screen.AlarmDetail.createRoute(alarmId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToMusicSelection = {
                    navController.navigate(Screen.Playlist.route) // Change to Playlist
                },
                onNavigateToNewAlarm = {
                    navController.navigate(Screen.AlarmDetail.createRoute(0L))
                },
            )
        }
        composable(
            route = Screen.AlarmDetail.route,
            arguments = listOf(
                navArgument("alarmId") { type = NavType.LongType },
            ),
        ) {
            AlarmDetailScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.MusicSelection.route) {
            com.tungnk123.soundalarm.presentation.music.MusicSelectionScreen()
        }
        composable(Screen.Playlist.route) {
            com.tungnk123.soundalarm.presentation.playlist.PlaylistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMusic = { navController.navigate(Screen.MusicSelection.route) }
            )
        }
    }
}
