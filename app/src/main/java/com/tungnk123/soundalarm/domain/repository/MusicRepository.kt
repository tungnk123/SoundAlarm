package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.MusicTrack
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    suspend fun getLocalMusicTracks(): List<MusicTrack>
}
