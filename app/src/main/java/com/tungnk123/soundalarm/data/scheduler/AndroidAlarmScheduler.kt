package com.tungnk123.soundalarm.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler
import com.tungnk123.soundalarm.presentation.scheduler.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
        }
        
        // Use alarm.id.toInt() for requestCode. Ensure it is unique.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next alarm time
        val now = LocalDateTime.now()
        var alarmTime = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)

        if (alarmTime.isBefore(now)) {
            alarmTime = alarmTime.plusDays(1)
        }
        
        // Handle repeat logic if needed currently or just schedule next instance
        // For simplicity, we just schedule the *next* occurrence.
        // If alarm has repeat days, we need to find the next matching day.
        if (alarm.repeatDays.isNotEmpty()) {
             // Logic to find next matching day
             // ... avoiding complex logic for now, assuming simple daily or next occurrence for this step is enough for the prototype
             // Refinement: If today doesn't match and it's already past time, or today doesn't match at all...
             // Let's stick to simple logic: "Next occurrence" logic from TimeFormatter? 
             // Ideally we find the next valid day.
             while (!alarm.repeatDays.contains(com.tungnk123.soundalarm.domain.model.DayOfWeek.valueOf(alarmTime.dayOfWeek.name)) && alarm.repeatDays.isNotEmpty()) {
                 alarmTime = alarmTime.plusDays(1)
             }
        }

        val millis = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    millis,
                    pendingIntent
                )
            } else {
                 // Fallback or request permission? 
                 // For now assumes permission is/will be granted.
                 alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    millis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                millis,
                pendingIntent
            )
        }
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
