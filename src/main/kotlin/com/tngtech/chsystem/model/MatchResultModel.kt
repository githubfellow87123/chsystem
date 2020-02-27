package com.tngtech.chsystem.model

import java.util.*

data class MatchResultModel(
    val id: UUID,
    val winsPlayer1: Int,
    val winsPlayer2: Int
)