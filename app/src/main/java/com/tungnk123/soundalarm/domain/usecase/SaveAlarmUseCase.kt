package com.tungnk123.soundalarm.domain.usecase

import com.tungnk123.soundalarm.domain.model.Alarm
import com.tungnk123.soundalarm.domain.repository.AlarmRepository
import javax.inject.Inject

class SaveAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: com.tungnk123.soundalarm.domain.scheduler.AlarmScheduler,
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        val id = if (alarm.id == 0L) {
            repository.insertAlarm(alarm)
        } else {
            repository.updateAlarm(alarm)
            alarm.id
        }
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.isEnabled) {
            scheduler.schedule(savedAlarm)
        } else {
            scheduler.cancel(savedAlarm)
        }
        return id
    }
}
