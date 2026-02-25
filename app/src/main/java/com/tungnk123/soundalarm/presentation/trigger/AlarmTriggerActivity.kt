package com.tungnk123.soundalarm.presentation.trigger

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tungnk123.soundalarm.presentation.service.AlarmService
import com.tungnk123.soundalarm.ui.theme.SoundAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmTriggerActivity : ComponentActivity() {

    private val viewModel: AlarmTriggerViewModel by viewModels()

    private val alarmStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AlarmService.ACTION_ALARM_STOPPED) {
                finish()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val alarmLabel = intent?.getStringExtra(AlarmService.EXTRA_ALARM_LABEL) ?: "Alarm"
        val alarmId = intent?.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1L) ?: -1L

        viewModel.loadAlarm(alarmId)
        viewModel.loadNextAlarm(excludeAlarmId = alarmId)

        val filter = IntentFilter(AlarmService.ACTION_ALARM_STOPPED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmStoppedReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(alarmStoppedReceiver, filter)
        }

        setContent {
            SoundAlarmTheme {
                val nextAlarmText by viewModel.nextAlarmText.collectAsStateWithLifecycle()
                val challengeConfig by viewModel.challengeConfig.collectAsStateWithLifecycle()
                AlarmTriggerScreen(
                    alarmLabel = alarmLabel,
                    nextAlarmText = nextAlarmText,
                    challengeConfig = challengeConfig,
                    onStop = { stopAlarm(alarmId, alarmLabel) },
                    onSnooze = { snoozeAlarm(alarmId, alarmLabel) },
                )
            }
        }
    }

    private fun stopAlarm(alarmId: Long, alarmLabel: String) {
        val dismissMethod = viewModel.challengeConfig.value.dismissMethod
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmService.EXTRA_DISMISS_METHOD, dismissMethod.name)
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
