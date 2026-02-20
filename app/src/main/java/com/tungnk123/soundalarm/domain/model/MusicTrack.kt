package com.tungnk123.soundalarm.domain.model

import android.net.Uri

data class MusicTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val contentUri: Uri,
    val isFavorite: Boolean = false,
    val playlistGroupId: Long = 0,
)
