package com.tngtech.chsystem.model

import java.util.*

data class MatchModel(
    val id: UUID,
    val roundIndex: Int,
    val player1: PlayerModel,
    val player2: PlayerModel
)