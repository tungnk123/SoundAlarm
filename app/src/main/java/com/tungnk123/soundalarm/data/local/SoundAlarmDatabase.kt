package com.tungnk123.soundalarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tungnk123.soundalarm.data.local.dao.AlarmDao
import com.tungnk123.soundalarm.data.local.dao.AlarmDayTrackDao
import com.tungnk123.soundalarm.data.local.dao.PlaylistDao
import com.tungnk123.soundalarm.data.local.dao.PlaylistGroupDao
import com.tungnk123.soundalarm.data.local.entity.AlarmDayTrackEntity
import com.tungnk123.soundalarm.data.local.entity.AlarmEntity
import com.tungnk123.soundalarm.data.local.entity.PlaylistEntity
import com.tungnk123.soundalarm.data.local.entity.PlaylistGroupEntity

@Database(
    entities = [AlarmEntity::class, PlaylistEntity::class, AlarmDayTrackEntity::class, PlaylistGroupEntity::class],
    version = 6,
    exportSchema = true,
)
abstract class SoundAlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun alarmDayTrackDao(): AlarmDayTrackDao
    abstract fun playlistGroupDao(): PlaylistGroupDao
}
