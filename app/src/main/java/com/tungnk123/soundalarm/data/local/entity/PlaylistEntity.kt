package com.tungnk123.soundalarm.data.local.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tungnk123.soundalarm.domain.model.MusicTrack

@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val duration: Long,
    val contentUri: String,
    val sortOrder: Int = 0,
) {
    fun toDomain(): MusicTrack = MusicTrack(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        contentUri = Uri.parse(contentUri)
    )

    companion object {
        fun fromDomain(track: MusicTrack, sortOrder: Int = 0): PlaylistEntity = PlaylistEntity(
            title = track.title,
            artist = track.artist,
            duration = track.duration,
            contentUri = track.contentUri.toString(),
            sortOrder = sortOrder,
        )
    }
}
