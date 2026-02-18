package com.tungnk123.soundalarm.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tungnk123.soundalarm.MainActivity
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import com.tungnk123.soundalarm.presentation.music.MusicAudioPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    @Inject
    lateinit var audioPlayer: MusicAudioPlayer

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_ALARM") {
            stopMusic()
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        
        startForeground(NOTIFICATION_ID, createNotification(alarmLabel))

        serviceScope.launch {
            playRandomTrack()
        }

        return START_STICKY
    }

    private fun stopMusic() {
        audioPlayer.stop()
    }

    private suspend fun playRandomTrack() {
        val tracks = playlistRepository.getPlaylistTracks()
        if (tracks.isNotEmpty()) {
            val randomTrack = tracks.random()
            // We need to run on Main thread to control ExoPlayer usually? 
            // MusicAudioPlayer methods should handle it or be thread safe. 
            // ExoPlayer must be accessed on the application looper (main thread usually).
            // Let's switch context.
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                audioPlayer.play(randomTrack.contentUri)
            }
        } else {
            // Fallback sound if playlist empty?
            // For now, doing nothing or maybe playing default.
        }
    }

    private fun createNotification(label: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Add "Stop" action
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm: $label")
            .setContentText("Playing music...")
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon or alarm icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent) // Need a stop icon, using launcher fg for now
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Alarm Notifications"
                setSound(null, null) // We play sound manually
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    // Handle Stop action
    // Wait, onStartCommand handles intent. We need to check action.
    // ... updating onStartCommand logic
}
