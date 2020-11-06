package com.tngtech.chsystem.model

import java.util.*

data class MatchModel(
    val id: UUID,
    val playerName1: String,
    val playerName2: String?,
    val roundIndex: Int
)
