package com.tungnk123.soundalarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tungnk123.soundalarm.domain.model.MusicTrack
import android.net.Uri

@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val duration: Long,
    val contentUri: String
) {
    fun toDomain(): MusicTrack = MusicTrack(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        contentUri = Uri.parse(contentUri)
    )

    companion object {
        fun fromDomain(track: MusicTrack): PlaylistEntity = PlaylistEntity(
            title = track.title,
            artist = track.artist,
            duration = track.duration,
            contentUri = track.contentUri.toString()
        )
    }
}
