package com.tungnk123.soundalarm.data.remote.api

import com.tungnk123.soundalarm.data.remote.dto.AlarmSoundDto
import retrofit2.http.GET

interface AlarmApiService {

    @GET("sounds")
    suspend fun getAlarmSounds(): List<AlarmSoundDto>
}
