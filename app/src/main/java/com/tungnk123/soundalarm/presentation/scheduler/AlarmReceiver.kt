package com.tungnk123.soundalarm.presentation.scheduler

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tungnk123.soundalarm.R
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
            ACTION_PRE_ALARM_NOTIFICATION -> showPreAlarmNotification(context, intent)
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
        snoozeManager.resetSnoozeCount(alarmId)
    }

    private fun showPreAlarmNotification(context: Context, intent: Intent) {
        val alarmLabel = intent.getStringExtra(AlarmService.EXTRA_ALARM_LABEL) ?: "Alarm"
        val minutesBefore = intent.getIntExtra(EXTRA_MINUTES_BEFORE, 10)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        ensurePreAlarmChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, PRE_ALARM_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_pre_alarm_title))
            .setContentText(context.getString(R.string.notification_pre_alarm_text, alarmLabel, minutesBefore))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(PRE_ALARM_NOTIFICATION_ID, notification)
    }

    private fun ensurePreAlarmChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            PRE_ALARM_CHANNEL_ID,
            "Pre-Alarm Reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications shown before an alarm rings"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun handleAlarmFired(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1L)
        val alarmLabel = intent.getStringExtra(AlarmService.EXTRA_ALARM_LABEL) ?: "Alarm"

        if (alarmId == -1L) return

        // If there is no active snooze, this is a fresh alarm fire — reset the snooze count
        // so the configured limit applies cleanly for this alarm session.
        val isFromSnooze = snoozeManager.snoozeState.value != null
        snoozeManager.clearSnooze()
        if (!isFromSnooze) {
            snoozeManager.resetSnoozeCount(alarmId)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(AlarmService.SNOOZE_NOTIFICATION_ID)
        notificationManager.cancel(PRE_ALARM_NOTIFICATION_ID)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarmLabel)
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

    companion object {
        const val ACTION_PRE_ALARM_NOTIFICATION = "com.tungnk123.soundalarm.PRE_ALARM_NOTIFICATION"
        const val EXTRA_MINUTES_BEFORE = "MINUTES_BEFORE"
        const val PRE_ALARM_CHANNEL_ID = "PRE_ALARM_CHANNEL"
        const val PRE_ALARM_NOTIFICATION_ID = 3
    }
}
