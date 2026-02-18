package com.tungnk123.soundalarm.presentation.service

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
import androidx.core.app.NotificationCompat
import com.tungnk123.soundalarm.MainActivity
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.repository.AlarmDayTrackRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import com.tungnk123.soundalarm.presentation.music.MusicAudioPlayer
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

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    @Inject
    lateinit var alarmDayTrackRepository: AlarmDayTrackRepository

    @Inject
    lateinit var audioPlayer: MusicAudioPlayer

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_LABEL = "ALARM_LABEL"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            stopMusic()
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val alarmLabel = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "Alarm"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(alarmLabel), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, createNotification(alarmLabel))
        }

        serviceScope.launch {
            playTrackForAlarm(alarmId)
        }

        return START_STICKY
    }

    private fun stopMusic() {
        audioPlayer.stop()
    }

    /**
     * Resolves the track to play:
     * 1. Check for a day-specific track for today's DayOfWeek
     * 2. Fall back to the DEFAULT track for this alarm
     * 3. Fall back to a random track from the global playlist
     */
    private suspend fun playTrackForAlarm(alarmId: Long) {
        val uriToPlay: Uri? = if (alarmId != -1L) {
            val dayTracks = alarmDayTrackRepository.getTracksForAlarmSync(alarmId)
            val todayName = LocalDate.now().dayOfWeek.toAlarmDay()
            val todayTrack = dayTracks.firstOrNull { it.dayOfWeek == todayName }
            val defaultTrack = dayTracks.firstOrNull { it.dayOfWeek == AlarmDayTrack.DEFAULT_DAY }
            val chosen = todayTrack ?: defaultTrack
            chosen?.let { Uri.parse(it.trackUri) }
        } else null

        val finalUri: Uri? = uriToPlay ?: run {
            val tracks = playlistRepository.getPlaylistTracks()
            tracks.randomOrNull()?.contentUri
        }

        finalUri?.let { uri ->
            withContext(Dispatchers.Main) {
                audioPlayer.play(uri)
            }
        }
    }

    private fun createNotification(label: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm: $label")
            .setContentText("Playing music...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Channel for Alarm Notifications"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

/** Maps java.time.DayOfWeek to the app's DayOfWeek enum name used in AlarmDayTrackEntity. */
private fun DayOfWeek.toAlarmDay(): String = when (this) {
    DayOfWeek.MONDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.MONDAY.name
    DayOfWeek.TUESDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.TUESDAY.name
    DayOfWeek.WEDNESDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.WEDNESDAY.name
    DayOfWeek.THURSDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.THURSDAY.name
    DayOfWeek.FRIDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.FRIDAY.name
    DayOfWeek.SATURDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.SATURDAY.name
    DayOfWeek.SUNDAY -> com.tungnk123.soundalarm.domain.model.DayOfWeek.SUNDAY.name
}
