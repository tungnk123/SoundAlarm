package com.tungnk123.soundalarm.presentation.music

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var fadeJob: Job? = null

    fun play(uri: Uri, volume: Float = 1.0f, fadeInDuration: Int = 0) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        exoPlayer?.apply {
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            if (fadeInDuration > 0) {
                this.volume = 0f
            } else {
                this.volume = volume.coerceIn(0f, 1f)
            }
            play()
        }

        if (fadeInDuration > 0) {
            startFadeIn(targetVolume = volume.coerceIn(0f, 1f), durationMs = fadeInDuration * 1000L)
        }
    }

    private fun startFadeIn(targetVolume: Float, durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = CoroutineScope(Dispatchers.IO).launch {
            val steps = 50
            val stepDelay = durationMs / steps
            for (i in 1..steps) {
                val currentVolume = targetVolume * (i.toFloat() / steps)
                withContext(Dispatchers.Main) {
                    exoPlayer?.volume = currentVolume
                }
                delay(stepDelay)
            }
            withContext(Dispatchers.Main) {
                exoPlayer?.volume = targetVolume
            }
        }
    }

    fun stop() {
        fadeJob?.cancel()
        fadeJob = null
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun release() {
        fadeJob?.cancel()
        fadeJob = null
        exoPlayer?.release()
        exoPlayer = null
    }
}
