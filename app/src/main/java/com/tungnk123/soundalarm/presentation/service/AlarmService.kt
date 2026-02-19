package com.tungnk123.soundalarm.presentation.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.repository.AlarmDayTrackRepository
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import com.tungnk123.soundalarm.presentation.music.MusicAudioPlayer
import com.tungnk123.soundalarm.presentation.scheduler.AlarmReceiver
import com.tungnk123.soundalarm.presentation.trigger.AlarmTriggerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import com.tungnk123.soundalarm.domain.model.DayOfWeek as AppDayOfWeek

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    @Inject
    lateinit var alarmDayTrackRepository: AlarmDayTrackRepository

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var audioPlayer: MusicAudioPlayer

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "SNOOZE_ALARM"
        const val ACTION_ALARM_STOPPED = "com.tungnk123.soundalarm.ALARM_STOPPED"
        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_LABEL = "ALARM_LABEL"

        private const val INVALID_ALARM_ID = -1L
        private const val DEFAULT_ALARM_LABEL = "Alarm"
        private const val SNOOZE_DURATION_MS = 10 * 60 * 1000L
        private const val SNOOZE_REQUEST_CODE_MULTIPLIER = 1000
        private const val SNOOZE_REQUEST_CODE_OFFSET = 999
        private const val TRIGGER_REQUEST_CODE = 0
        private const val STOP_REQUEST_CODE = 0
        private const val SNOOZE_REQUEST_CODE = 1
        private const val NO_ICON = 0
        private const val CHANNEL_NAME = "Alarm Channel"
        private const val CHANNEL_DESCRIPTION = "Channel for Alarm Notifications"
        private const val NOTIFICATION_CONTENT_TEXT = "Tap to view alarm"
        private const val ACTION_LABEL_SNOOZE = "Snooze"
        private const val ACTION_LABEL_STOP = "Stop"

        private val VIBRATION_PATTERN = longArrayOf(0, 500, 500)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                stopAlarmAndNotify()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, INVALID_ALARM_ID)
                val alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: DEFAULT_ALARM_LABEL
                scheduleSnooze(alarmId, alarmLabel)
                stopAlarmAndNotify()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, INVALID_ALARM_ID) ?: INVALID_ALARM_ID
        val alarmLabel = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: DEFAULT_ALARM_LABEL

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(alarmId, alarmLabel),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification(alarmId, alarmLabel))
        }

        serviceScope.launch {
            val alarm = if (alarmId != INVALID_ALARM_ID) alarmRepository.getAlarmById(alarmId) else null
            if (alarm?.isVibrate != false) {
                withContext(Dispatchers.Main) { startVibration() }
            }
            playTrackForAlarm(alarmId)
        }

        return START_STICKY
    }

    private fun startVibration() {
        vibrator?.vibrate(
            VibrationEffect.createWaveform(VIBRATION_PATTERN, 0),
        )
    }

    private fun stopAlarmAndNotify() {
        vibrator?.cancel()
        audioPlayer.stop()
        sendBroadcast(Intent(ACTION_ALARM_STOPPED).apply {
            setPackage(packageName)
        })
    }

    private fun scheduleSnooze(alarmId: Long, alarmLabel: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, alarmLabel)
        }
        val requestCode = (alarmId * SNOOZE_REQUEST_CODE_MULTIPLIER + SNOOZE_REQUEST_CODE_OFFSET).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val snoozeAtMillis = System.currentTimeMillis() + SNOOZE_DURATION_MS
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeAtMillis, pendingIntent)
    }

    private suspend fun playTrackForAlarm(alarmId: Long) {
        val uriToPlay: Uri? = if (alarmId != INVALID_ALARM_ID) {
            val dayTracks = alarmDayTrackRepository.getTracksForAlarmSync(alarmId)
            val todayName = LocalDate.now().dayOfWeek.toAlarmDay()
            val todayTrack = dayTracks.firstOrNull { it.dayOfWeek == todayName }
            val defaultTrack = dayTracks.firstOrNull { it.dayOfWeek == AlarmDayTrack.DEFAULT_DAY }
            (todayTrack ?: defaultTrack)?.trackUri?.toUri()
        } else null

        val finalUri: Uri? = uriToPlay ?: playlistRepository.getPlaylistTracks().randomOrNull()?.contentUri

        finalUri?.let { uri ->
            withContext(Dispatchers.Main) {
                audioPlayer.play(uri)
            }
        }
    }

    private fun createNotification(alarmId: Long, label: String): Notification {
        val triggerIntent = Intent(this, AlarmTriggerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, TRIGGER_REQUEST_CODE, triggerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this, STOP_REQUEST_CODE, stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this, SNOOZE_REQUEST_CODE, snoozeIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm: $label")
            .setContentText(NOTIFICATION_CONTENT_TEXT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(NO_ICON, ACTION_LABEL_SNOOZE, snoozePendingIntent)
            .addAction(NO_ICON, ACTION_LABEL_STOP, stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESCRIPTION
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

private fun DayOfWeek.toAlarmDay(): String = when (this) {
    DayOfWeek.MONDAY -> AppDayOfWeek.MONDAY.name
    DayOfWeek.TUESDAY -> AppDayOfWeek.TUESDAY.name
    DayOfWeek.WEDNESDAY -> AppDayOfWeek.WEDNESDAY.name
    DayOfWeek.THURSDAY -> AppDayOfWeek.THURSDAY.name
    DayOfWeek.FRIDAY -> AppDayOfWeek.FRIDAY.name
    DayOfWeek.SATURDAY -> AppDayOfWeek.SATURDAY.name
    DayOfWeek.SUNDAY -> AppDayOfWeek.SUNDAY.name
}
