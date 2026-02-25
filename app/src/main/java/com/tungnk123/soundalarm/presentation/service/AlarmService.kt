package com.tungnk123.soundalarm.presentation.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.tungnk123.soundalarm.R
import com.tungnk123.soundalarm.domain.model.AlarmDayTrack
import com.tungnk123.soundalarm.domain.model.AlarmEventType
import com.tungnk123.soundalarm.domain.model.AppSettings
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.domain.repository.AlarmDayTrackRepository
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import com.tungnk123.soundalarm.domain.repository.SettingsRepository
import com.tungnk123.soundalarm.domain.repository.StatisticsRepository
import com.tungnk123.soundalarm.domain.snooze.SnoozeManager
import com.tungnk123.soundalarm.presentation.music.MusicAudioPlayer
import com.tungnk123.soundalarm.presentation.scheduler.AlarmReceiver
import com.tungnk123.soundalarm.presentation.trigger.AlarmTriggerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import java.util.Locale
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

    @Inject
    lateinit var snoozeManager: SnoozeManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var statisticsRepository: StatisticsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vibrator: Vibrator? = null
    private var autoStopJob: Job? = null
    private var tts: TextToSpeech? = null
    private var customVoicePlayer: MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val SNOOZE_CHANNEL_ID = "SNOOZE_CHANNEL"
        const val NOTIFICATION_ID = 1
        const val SNOOZE_NOTIFICATION_ID = 2
        const val ACTION_STOP_ALARM = "STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "SNOOZE_ALARM"
        const val ACTION_CANCEL_SNOOZE = "CANCEL_SNOOZE"
        const val ACTION_ALARM_STOPPED = "com.tungnk123.soundalarm.ALARM_STOPPED"
        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_ALARM_LABEL = "ALARM_LABEL"
        const val EXTRA_DISMISS_METHOD = "DISMISS_METHOD"

        private const val INVALID_ALARM_ID = -1L
        private const val SNOOZE_DURATION_MS = 10 * 60 * 1000L
        const val SNOOZE_REQUEST_CODE_MULTIPLIER = 1000
        const val SNOOZE_REQUEST_CODE_OFFSET = 999
        private const val TRIGGER_REQUEST_CODE = 0
        private const val STOP_REQUEST_CODE = 0
        private const val SNOOZE_REQUEST_CODE = 1
        private const val CANCEL_SNOOZE_REQUEST_CODE = 2
        private const val NO_ICON = 0

        private val VIBRATION_PATTERN = longArrayOf(0, 600, 400)
        private val VIBRATION_AMPLITUDES = intArrayOf(0, 255, 0)
        private const val TTS_UTTERANCE_ID = "alarm_time_announcement"
        const val TIME_PLACEHOLDER = "{time}"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createSnoozeNotificationChannel()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, INVALID_ALARM_ID)
                val alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL).orEmpty()
                val dismissMethod = runCatching {
                    DismissMethod.valueOf(intent.getStringExtra(EXTRA_DISMISS_METHOD) ?: "NONE")
                }.getOrDefault(DismissMethod.NONE)
                serviceScope.launch {
                    if (alarmId != INVALID_ALARM_ID) {
                        val label = alarmLabel.ifEmpty {
                            alarmRepository.getAlarmById(alarmId)?.label.orEmpty()
                        }
                        statisticsRepository.recordEvent(
                            alarmId = alarmId,
                            alarmLabel = label,
                            eventType = AlarmEventType.DISMISSED,
                            dismissMethod = dismissMethod,
                        )
                    }
                }
                snoozeManager.resetSnoozeCount(alarmId)
                autoStopJob?.cancel()
                stopAlarmAndNotify()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE_ALARM -> {
                val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, INVALID_ALARM_ID)
                val alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: getString(R.string.label_alarm_default)
                serviceScope.launch {
                    if (alarmId != INVALID_ALARM_ID) {
                        statisticsRepository.recordEvent(
                            alarmId = alarmId,
                            alarmLabel = alarmLabel,
                            eventType = AlarmEventType.SNOOZED,
                        )
                    }
                }
                val settings = settingsRepository.getSettings()
                val snoozeCount = snoozeManager.getSnoozeCount(alarmId)
                val maxSnooze = settings.snoozeCount
                if (maxSnooze == 0 || snoozeCount < maxSnooze) {
                    scheduleSnooze(alarmId, alarmLabel)
                }
                autoStopJob?.cancel()
                stopAlarmAndNotify()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, INVALID_ALARM_ID) ?: INVALID_ALARM_ID
        val alarmLabel = intent?.getStringExtra(EXTRA_ALARM_LABEL).orEmpty()

        startForeground(
            NOTIFICATION_ID,
            createNotification(alarmId, alarmLabel),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
        )

        val settings = settingsRepository.getSettings()
        val durationMs = settings.alarmDuration * 60 * 1000L
        autoStopJob = serviceScope.launch {
            delay(durationMs)
            withContext(Dispatchers.Main) {
                snoozeManager.resetSnoozeCount(alarmId)
                stopAlarmAndNotify()
                stopSelf()
            }
        }

        serviceScope.launch {
            if (alarmId != INVALID_ALARM_ID) {
                statisticsRepository.recordEvent(
                    alarmId = alarmId,
                    alarmLabel = alarmLabel,
                    eventType = AlarmEventType.FIRED,
                )
            }
        }

        serviceScope.launch {
            val alarm = if (alarmId != INVALID_ALARM_ID) alarmRepository.getAlarmById(alarmId) else null
            if (alarm?.isVibrate != false) {
                withContext(Dispatchers.Main) { startVibration() }
            }
            val customVoicePath = settings.customVoiceAudioPath
            if (settings.readTimeAloud && customVoicePath.isEmpty()) {
                withContext(Dispatchers.Main) { speakCurrentTime() }
            }
            if (customVoicePath.isNotEmpty() && settings.voiceBeforeMusic) {
                withContext(Dispatchers.Main) {
                    playCustomVoiceAudioThenMusic(
                        path = customVoicePath,
                        alarmId = alarmId,
                        isRandom = alarm?.isRandomMusic == true,
                        volume = alarm?.volume ?: 1.0f,
                        fadeInDuration = alarm?.fadeInDuration ?: 0,
                        playlistGroupId = alarm?.playlistGroupId ?: 0L,
                    )
                }
            } else {
                if (customVoicePath.isNotEmpty()) {
                    withContext(Dispatchers.Main) { playCustomVoiceAudio(customVoicePath) }
                }
                playTrackForAlarm(
                    alarmId = alarmId,
                    isRandom = alarm?.isRandomMusic == true,
                    volume = alarm?.volume ?: 1.0f,
                    fadeInDuration = alarm?.fadeInDuration ?: 0,
                    playlistGroupId = alarm?.playlistGroupId ?: 0L,
                )
            }
        }

        return START_STICKY
    }

    private fun speakCurrentTime() {
        val cal = java.util.Calendar.getInstance()
        val hour24 = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = cal.get(java.util.Calendar.MINUTE)
        val spokenTime = buildSpokenTime(hour24, minute)

        val settings = settingsRepository.getSettings()
        val template = settings.timeAnnouncementTemplate
            .ifBlank { AppSettings.DEFAULT_TIME_ANNOUNCEMENT_TEMPLATE }
        val announcement = template.replace(TIME_PLACEHOLDER, spokenTime)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                val params = Bundle().apply {
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM)
                }
                tts?.speak(announcement, TextToSpeech.QUEUE_FLUSH, params, TTS_UTTERANCE_ID)
            }
        }
    }

    private fun playCustomVoiceAudio(path: String) {
        val file = java.io.File(path)
        if (!file.exists()) return
        try {
            customVoicePlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(path)
                prepare()
                start()
            }
        } catch (_: Exception) {
        }
    }

    private fun playCustomVoiceAudioThenMusic(
        path: String,
        alarmId: Long,
        isRandom: Boolean,
        volume: Float,
        fadeInDuration: Int,
        playlistGroupId: Long,
    ) {
        val file = java.io.File(path)
        if (!file.exists()) {
            serviceScope.launch { playTrackForAlarm(alarmId, isRandom, volume, fadeInDuration, playlistGroupId) }
            return
        }
        try {
            customVoicePlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(path)
                setOnCompletionListener {
                    serviceScope.launch { playTrackForAlarm(alarmId, isRandom, volume, fadeInDuration, playlistGroupId) }
                }
                prepare()
                start()
            }
        } catch (_: Exception) {
            serviceScope.launch { playTrackForAlarm(alarmId, isRandom, volume, fadeInDuration, playlistGroupId) }
        }
    }

    private fun buildSpokenTime(hour24: Int, minute: Int): String {
        val isPm = hour24 >= 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val amPm = if (isPm) "PM" else "AM"
        return if (minute == 0) {
            "$hour12 o'clock $amPm"
        } else {
            "$hour12 hours $minute minutes $amPm"
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startVibration() {
        val effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, VIBRATION_AMPLITUDES, 0)
        val attrs = VibrationAttributes.Builder()
            .setUsage(VibrationAttributes.USAGE_ALARM)
            .build()
        vibrator?.vibrate(effect, attrs)
    }

    private fun stopAlarmAndNotify() {
        vibrator?.cancel()
        audioPlayer.stop()
        customVoicePlayer?.stop()
        customVoicePlayer?.release()
        customVoicePlayer = null
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeAtMillis, pendingIntent)
        }

        snoozeManager.setSnooze(alarmId, alarmLabel, snoozeAtMillis)
        showSnoozeNotification(alarmId, alarmLabel, snoozeAtMillis)
    }

    private fun showSnoozeNotification(alarmId: Long, alarmLabel: String, snoozeUntilMs: Long) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(snoozeUntilMs))

        val cancelSnoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = ACTION_CANCEL_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, alarmLabel)
        }
        val cancelSnoozePendingIntent = PendingIntent.getBroadcast(
            this,
            CANCEL_SNOOZE_REQUEST_CODE,
            cancelSnoozeIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, SNOOZE_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_snooze_title))
            .setContentText(getString(R.string.notification_snooze_text, alarmLabel, timeStr))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .addAction(NO_ICON, getString(R.string.notification_action_cancel_snooze), cancelSnoozePendingIntent)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(SNOOZE_NOTIFICATION_ID, notification)
    }

    private suspend fun playTrackForAlarm(
        alarmId: Long,
        isRandom: Boolean = false,
        volume: Float = 1.0f,
        fadeInDuration: Int = 0,
        playlistGroupId: Long = 0,
    ) {
        val uriToPlay: Uri? = if (!isRandom && alarmId != INVALID_ALARM_ID) {
            val dayTracks = alarmDayTrackRepository.getTracksForAlarmSync(alarmId)
            val todayName = LocalDate.now().dayOfWeek.toAlarmDay()
            val todayTrack = dayTracks.firstOrNull { it.dayOfWeek == todayName }
            val defaultTrack = dayTracks.firstOrNull { it.dayOfWeek == AlarmDayTrack.DEFAULT_DAY }
            (todayTrack ?: defaultTrack)?.trackUri?.toUri()
        } else null

        val randomTrack = if (playlistGroupId != 0L) {
            playlistRepository.getTracksByPlaylistGroupList(playlistGroupId).randomOrNull()?.contentUri
        } else {
            playlistRepository.getPlaylistTracks().randomOrNull()?.contentUri
        }
        val finalUri: Uri? = uriToPlay ?: randomTrack

        finalUri?.let { uri ->
            withContext(Dispatchers.Main) {
                audioPlayer.play(uri, volume = volume, fadeInDuration = fadeInDuration)
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
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
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
            .setContentTitle(getString(R.string.notification_alarm_title, label))
            .setContentText(getString(R.string.notification_alarm_content))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(NO_ICON, getString(R.string.notification_action_snooze), snoozePendingIntent)
            .addAction(NO_ICON, getString(R.string.notification_action_stop), stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_alarm_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.notification_channel_alarm_desc)
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createSnoozeNotificationChannel() {
        val channel = NotificationChannel(
            SNOOZE_CHANNEL_ID,
            getString(R.string.notification_channel_snooze_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_snooze_desc)
            setSound(null, null)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        autoStopJob?.cancel()
        serviceScope.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        customVoicePlayer?.stop()
        customVoicePlayer?.release()
        customVoicePlayer = null
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
