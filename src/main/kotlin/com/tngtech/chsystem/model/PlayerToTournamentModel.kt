package com.tngtech.chsystem.model

import java.util.*

data class PlayerToTournamentModel(
    val tournamentId: UUID,
    val playerId: UUID
)