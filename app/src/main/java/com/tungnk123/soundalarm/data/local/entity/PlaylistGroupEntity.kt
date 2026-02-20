package com.tungnk123.soundalarm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tungnk123.soundalarm.domain.model.PlaylistGroup

@Entity(tableName = "playlist_groups")
data class PlaylistGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun toDomain(trackCount: Int = 0) = PlaylistGroup(
        id = id,
        name = name,
        trackCount = trackCount,
    )
}
