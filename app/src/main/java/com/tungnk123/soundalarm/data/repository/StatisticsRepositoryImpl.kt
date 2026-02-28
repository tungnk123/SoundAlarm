package com.tungnk123.soundalarm.data.repository

import com.tungnk123.soundalarm.data.local.dao.AlarmEventDao
import com.tungnk123.soundalarm.data.local.entity.AlarmEventEntity
import com.tungnk123.soundalarm.domain.model.AlarmEventType
import com.tungnk123.soundalarm.domain.model.AlarmStatistics
import com.tungnk123.soundalarm.domain.model.DismissMethod
import com.tungnk123.soundalarm.domain.model.toDomain
import com.tungnk123.soundalarm.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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

    override fun getStatistics(): Flow<AlarmStatistics> {
        val firedFlow = alarmEventDao.countByEventType(AlarmEventType.FIRED.name)
        val dismissedFlow = alarmEventDao.countByEventType(AlarmEventType.DISMISSED.name)
        val snoozedFlow = alarmEventDao.countByEventType(AlarmEventType.SNOOZED.name)
        val recentFlow = alarmEventDao.getRecentEvents()
        val methodFlows = DismissMethod.entries.map { method ->
            combine(alarmEventDao.countDismissedByMethod(method.name), flowOf(method)) { count, m ->
                m to count
            }
        }
        val byMethodFlow = combine(methodFlows) { pairs -> pairs.toMap() }

        return combine(
            firedFlow,
            dismissedFlow,
            snoozedFlow,
            recentFlow,
            byMethodFlow,
        ) { fired, dismissed, snoozed, recent, byMethod ->
            AlarmStatistics(
                totalFired = fired,
                totalDismissed = dismissed,
                totalSnoozed = snoozed,
                dismissByMethod = byMethod,
                recentEvents = recent.map { it.toDomain() },
            )
        }
    }

    override suspend fun clearStatistics() {
        alarmEventDao.clearAllEvents()
    }
}
