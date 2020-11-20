package com.tngtech.chsystem.model

import java.util.*

data class MatchModel(
    val id: UUID,
    val tournamentId: UUID,
    val playerName1: String,
    val playerName2: String?,
    val winsPlayer1: Int?,
    val winsPlayer2: Int?,
    val roundIndex: Int
)
