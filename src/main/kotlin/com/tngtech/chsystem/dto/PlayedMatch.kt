package com.tngtech.chsystem.dto

import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import java.time.LocalDateTime
import java.util.*

data class PlayedMatch(
    val id: UUID,
    val tournament: TournamentEntity,
    val roundIndex: Int,
    val player1: PlayerEntity,
    val player2: PlayerEntity?,
    val winsPlayer1: Int,
    val winsPlayer2: Int,
    val lastUpdated: LocalDateTime
)