package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.dao.AlarmEventDao
import com.tungnk123.soundalarm.data.local.entity.AlarmEventEntity
import com.tungnk123.soundalarm.domain.model.AlarmEventType
import com.tungnk123.soundalarm.domain.model.AlarmStatistics
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.domain.model.toDomain
import com.tungnk123.soundalarm.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val alarmEventDao: AlarmEventDao,
) : StatisticsRepository {

    override suspend fun recordEvent(
        alarmId: Long,
        alarmLabel: String,
        eventType: AlarmEventType,
        dismissMethod: DismissMethod,
    ) {
        alarmEventDao.insertEvent(
            AlarmEventEntity(
                alarmId = alarmId,
                alarmLabel = alarmLabel,
                eventType = eventType.name,
                dismissMethod = dismissMethod.name,
            )
        )
    }

    override fun getStatistics(): Flow<AlarmStatistics> =
        alarmEventDao.getAllEvents().map { events ->
            val fired = events.count { it.eventType == AlarmEventType.FIRED.name }
            val dismissed = events.count { it.eventType == AlarmEventType.DISMISSED.name }
            val snoozed = events.count { it.eventType == AlarmEventType.SNOOZED.name }
            val byMethod = DismissMethod.entries.associateWith { method ->
                events.count {
                    it.eventType == AlarmEventType.DISMISSED.name && it.dismissMethod == method.name
                }
            }
            val recent = events.take(20).map { it.toDomain() }
            AlarmStatistics(
                totalFired = fired,
                totalDismissed = dismissed,
                totalSnoozed = snoozed,
                dismissByMethod = byMethod,
                recentEvents = recent,
            )
        }

    override suspend fun clearStatistics() {
        alarmEventDao.clearAllEvents()
    }
}
