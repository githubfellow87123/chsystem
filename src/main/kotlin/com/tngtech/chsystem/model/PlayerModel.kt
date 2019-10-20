package com.tngtech.chsystem.model

import java.time.LocalDateTime
import java.util.*

data class PlayerModel(
    val id: UUID,
    val name: String,
    val createdAt: LocalDateTime
)