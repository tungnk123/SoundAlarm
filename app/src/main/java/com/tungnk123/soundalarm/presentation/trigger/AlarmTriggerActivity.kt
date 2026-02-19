package com.tungnk123.soundalarm.presentation.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tungnk123.soundalarm.presentation.service.AlarmService
import com.tungnk123.soundalarm.ui.theme.SoundAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmTriggerActivity : ComponentActivity() {

    private val alarmStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AlarmService.ACTION_ALARM_STOPPED) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alarmLabel = intent?.getStringExtra(AlarmService.EXTRA_ALARM_LABEL) ?: "Alarm"
        val alarmId = intent?.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1L) ?: -1L

        val filter = IntentFilter(AlarmService.ACTION_ALARM_STOPPED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmStoppedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(alarmStoppedReceiver, filter)
        }

        setContent {
            SoundAlarmTheme {
                AlarmTriggerScreen(
                    alarmLabel = alarmLabel,
                    onStop = { stopAlarm() },
                    onSnooze = { snoozeAlarm(alarmId, alarmLabel) },
                )
            }
        }
    }

    private fun stopAlarm() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        startService(stopIntent)
        finish()
    }

    private fun snoozeAlarm(alarmId: Long, alarmLabel: String) {
        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_SNOOZE_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarmLabel)
        }
        startService(snoozeIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(alarmStoppedReceiver)
    }
}
