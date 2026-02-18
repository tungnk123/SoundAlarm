package com.tungnk123.soundalarm.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AlarmSoundDto(
    val id: String,
    val name: String,
    val url: String,
    val category: String,
)
