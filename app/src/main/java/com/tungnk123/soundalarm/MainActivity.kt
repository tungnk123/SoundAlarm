package com.tungnk123.soundalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.tungnk123.soundalarm.presentation.navigation.NavGraph
import com.tungnk123.soundalarm.ui.theme.SoundAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundAlarmTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
