package com.tungnk123.soundalarm.domain.repository

import com.tungnk123.soundalarm.domain.model.AlarmEventType
import com.tungnk123.soundalarm.domain.model.AlarmStatistics
import com.tungnk123.soundalarm.domain.model.DismissMethod
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun recordEvent(
        alarmId: Long,
        alarmLabel: String,
        eventType: AlarmEventType,
        dismissMethod: DismissMethod = DismissMethod.NONE,
    )

    fun getStatistics(): Flow<AlarmStatistics>

    suspend fun clearStatistics()
}
