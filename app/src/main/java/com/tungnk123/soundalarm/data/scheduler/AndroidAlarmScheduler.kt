package com.tungnk123.soundalarm.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.model.DayOfWeek as AppDayOfWeek
import com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler
import com.tungnk123.soundalarm.presentation.scheduler.AlarmReceiver
import com.tungnk123.soundalarm.presentation.service.AlarmService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val pendingIntent = buildPendingIntent(alarm)
        val triggerAtMillis = nextTriggerTime(alarm)

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

    override fun cancel(alarm: Alarm) {
        alarmManager.cancel(buildPendingIntent(alarm))
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
}
