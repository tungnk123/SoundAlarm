package com.tungnk123.soundalarm.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.tungnk123.soundalarm.data.local.SoundAlarmDatabase
import com.tungnk123.soundalarm.data.local.dao.AlarmDao
import com.tungnk123.soundalarm.data.local.dao.AlarmDayTrackDao
import com.tungnk123.soundalarm.data.local.dao.AlarmEventDao
import com.tungnk123.soundalarm.data.local.dao.PlaylistDao
import com.tungnk123.soundalarm.data.local.dao.PlaylistGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

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

    @Provides
    @Singleton
    fun provideAlarmDayTrackDao(database: SoundAlarmDatabase): AlarmDayTrackDao {
        return database.alarmDayTrackDao()
    }

    @Provides
    @Singleton
    fun providePlaylistGroupDao(database: SoundAlarmDatabase): PlaylistGroupDao {
        return database.playlistGroupDao()
    }

    @Provides
    @Singleton
    fun provideAlarmEventDao(database: SoundAlarmDatabase): AlarmEventDao {
        return database.alarmEventDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore
}
