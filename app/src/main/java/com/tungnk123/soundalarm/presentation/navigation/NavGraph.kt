package com.tungnk123.soundalarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tungnk123.soundalarm.presentation.alarm.AlarmDetailScreen
import com.tungnk123.soundalarm.presentation.challenge.ChallengeScreen
import com.tungnk123.soundalarm.presentation.home.HomeScreen
import com.tungnk123.soundalarm.presentation.music.MusicSelectionScreen
import com.tungnk123.soundalarm.presentation.playlist.PlaylistGroupsScreen
import com.tungnk123.soundalarm.presentation.playlist.PlaylistScreen
import com.tungnk123.soundalarm.presentation.settings.SettingsScreen
import com.tungnk123.soundalarm.presentation.statistics.StatisticsScreen

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
                onNavigateToNewAlarm = {
                    navController.navigate(Screen.AlarmDetail.createRoute(0L))
                },
                onNavigateToSounds = {
                    navController.navigate(Screen.Sounds.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(Screen.Sounds.route) {
            PlaylistGroupsScreen(
                onNavigateBack = {},
                showBackButton = false,
                onNavigateToPlaylistDetail = { playlistGroupId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistGroupId))
                },
            )
        }
        composable(Screen.Challenge.route) {
            ChallengeScreen()
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = Screen.AlarmDetail.route,
            arguments = listOf(
                navArgument("alarmId") { type = NavType.LongType },
            ),
        ) {
            AlarmDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlaylist = { navController.navigate(Screen.PlaylistGroups.route) },
            )
        }
        composable(
            route = Screen.MusicSelection.route,
            arguments = listOf(
                navArgument("playlistGroupId") { type = NavType.LongType },
            ),
        ) {
            MusicSelectionScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(Screen.PlaylistGroups.route) {
            PlaylistGroupsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlaylistDetail = { playlistGroupId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistGroupId))
                },
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(
                navArgument("playlistGroupId") { type = NavType.LongType },
            ),
        ) {
            PlaylistScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMusic = { playlistGroupId ->
                    navController.navigate(Screen.MusicSelection.createRoute(playlistGroupId))
                },
            )
        }
    }
}
