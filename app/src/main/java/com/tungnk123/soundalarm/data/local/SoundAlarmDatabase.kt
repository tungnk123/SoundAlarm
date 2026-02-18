package com.tungnk123.soundalarm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tungnk123.soundalarm.data.local.dao.AlarmDao
import com.tungnk123.soundalarm.data.local.dao.PlaylistDao
import com.tungnk123.soundalarm.data.local.entity.AlarmEntity
import com.tungnk123.soundalarm.data.local.entity.PlaylistEntity

@Database(
    entities = [AlarmEntity::class, PlaylistEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SoundAlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun playlistDao(): PlaylistDao
}
