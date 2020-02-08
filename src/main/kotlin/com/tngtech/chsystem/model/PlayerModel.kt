package com.tngtech.chsystem.model

import java.time.LocalDateTime
import java.util.*

data class PlayerModel(
    val id: UUID? = null,
    val name: String,
    val createdAt: LocalDateTime? = null
)