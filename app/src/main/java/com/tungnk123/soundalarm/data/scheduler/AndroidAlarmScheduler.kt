package com.tungnk123.soundalarm.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek as AppDayOfWeek
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler
import com.tungnk123.soundalarm.presentation.scheduler.AlarmReceiver
import com.tungnk123.soundalarm.presentation.service.AlarmService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val triggerAtMillis = nextTriggerTime(alarm)
        val settings = settingsRepository.getSettings()

        if (settings.alarmWhenPowerOff) {
            val launchIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
                putExtra(AlarmService.EXTRA_ALARM_LABEL, alarm.label)
            }
            val launchPendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id.toInt(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, launchPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, launchPendingIntent)
        } else {
            val pendingIntent = buildPendingIntent(alarm)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }

        if (alarm.notifyBeforeMinutes > 0) {
            schedulePreAlarmNotification(alarm, triggerAtMillis, alarm.notifyBeforeMinutes)
        } else if (settings.preAlarmNotification) {
            schedulePreAlarmNotification(alarm, triggerAtMillis, settings.preAlarmNotificationTime)
        } else {
            cancelPreAlarmNotification(alarm)
        }
    }

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(buildPendingIntent(alarm))
        cancelPreAlarmNotification(alarm)
    }

    private fun schedulePreAlarmNotification(alarm: Alarm, triggerAtMillis: Long, minutesBefore: Int) {
        val preAlarmAtMillis = triggerAtMillis - minutesBefore * 60 * 1000L
        if (preAlarmAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRE_ALARM_NOTIFICATION
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarm.label)
            putExtra(AlarmReceiver.EXTRA_MINUTES_BEFORE, minutesBefore)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarm.id + PRE_ALARM_REQUEST_CODE_OFFSET).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preAlarmAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preAlarmAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preAlarmAtMillis, pendingIntent)
        }
    }

    private fun cancelPreAlarmNotification(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_PRE_ALARM_NOTIFICATION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (alarm.id + PRE_ALARM_REQUEST_CODE_OFFSET).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, alarm.label)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerTime(alarm: Alarm): Long {
        val now = LocalDateTime.now()
        var candidate = now
            .withHour(alarm.hour)
            .withMinute(alarm.minute)
            .withSecond(0)
            .withNano(0)

        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }

        if (alarm.repeatDays.isEmpty()) {
            return candidate.toEpochMilli()
        }

        for (i in 0 until 7) {
            if (AppDayOfWeek.valueOf(candidate.dayOfWeek.name) in alarm.repeatDays) {
                return candidate.toEpochMilli()
            }
            candidate = candidate.plusDays(1)
        }

        return candidate.toEpochMilli()
    }

    private fun LocalDateTime.toEpochMilli(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    companion object {
        private const val PRE_ALARM_REQUEST_CODE_OFFSET = 100_000
    }
}
