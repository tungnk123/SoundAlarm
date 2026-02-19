package com.tungnk123.soundalarm.di

import com.tungnk123.soundalarm.data.repository.AlarmDayTrackRepositoryImpl
import com.tungnk123.soundalarm.data.repository.AlarmRepositoryImpl
import com.tungnk123.soundalarm.data.repository.LocalMusicRepositoryImpl
import com.tungnk123.soundalarm.data.repository.PlaylistRepositoryImpl
import com.tungnk123.soundalarm.domain.repository.AlarmDayTrackRepository
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import com.tungnk123.soundalarm.domain.repository.MusicRepository
import com.tungnk123.soundalarm.domain.repository.PlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindMusicRepository(impl: LocalMusicRepositoryImpl): MusicRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: com.tungnk123.soundalarm.data.scheduler.AndroidAlarmScheduler): com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindAlarmDayTrackRepository(impl: AlarmDayTrackRepositoryImpl): AlarmDayTrackRepository
}
