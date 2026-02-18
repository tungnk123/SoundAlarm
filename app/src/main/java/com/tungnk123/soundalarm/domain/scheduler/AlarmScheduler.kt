package com.tungnk123.soundalarm.domain.scheduler

import com.tungnk123.soundalarm.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}
