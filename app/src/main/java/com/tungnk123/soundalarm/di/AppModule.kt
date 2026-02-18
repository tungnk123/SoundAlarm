package com.tungnk123.soundalarm.di

import android.content.Context
import androidx.room.Room
import com.tungnk123.soundalarm.data.local.SoundAlarmDatabase
import com.tungnk123.soundalarm.data.local.dao.AlarmDao
import com.tungnk123.soundalarm.data.local.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoundAlarmDatabase {
        return Room.databaseBuilder(
            context,
            SoundAlarmDatabase::class.java,
            "sound_alarm_db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: SoundAlarmDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: SoundAlarmDatabase): PlaylistDao {
        return database.playlistDao()
    }
}
