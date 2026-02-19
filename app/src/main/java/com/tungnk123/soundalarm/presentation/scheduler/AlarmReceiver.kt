package com.tungnk123.soundalarm.presentation.scheduler

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler
import com.tungnk123.soundalarm.domain.snooze.SnoozeManager
import com.tungnk123.soundalarm.presentation.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var snoozeManager: SnoozeManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> rescheduleAllEnabledAlarms()
            AlarmService.ACTION_CANCEL_SNOOZE -> cancelSnooze(context, intent)
            else -> handleAlarmFired(context, intent)
        }
    }

    private fun rescheduleAllEnabledAlarms() {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                alarmRepository.getEnabledAlarms().forEach { alarm ->
                    alarmScheduler.schedule(alarm)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun cancelSnooze(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1L)
        val alarmLabel = intent.getStringExtra(AlarmService.EXTRA_ALARM_LABEL) ?: "Alarm"
        if (alarmId == -1L) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarmLabel)
        }
        val requestCode = (alarmId * AlarmService.SNOOZE_REQUEST_CODE_MULTIPLIER + AlarmService.SNOOZE_REQUEST_CODE_OFFSET).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(AlarmService.SNOOZE_NOTIFICATION_ID)

        snoozeManager.clearSnooze()
    }

    private fun handleAlarmFired(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"

        if (alarmId == -1L) return

        // Clear any active snooze state when the alarm fires
        snoozeManager.clearSnooze()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(AlarmService.SNOOZE_NOTIFICATION_ID)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
        }
        context.startForegroundService(serviceIntent)
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                alarmRepository.getAlarmById(alarmId)?.let { alarm ->
                    when {
                        alarm.deleteAfterFired && alarm.repeatDays.isEmpty() ->
                            alarmRepository.deleteAlarm(alarm)
                        alarm.repeatDays.isNotEmpty() ->
                            alarmScheduler.schedule(alarm)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
