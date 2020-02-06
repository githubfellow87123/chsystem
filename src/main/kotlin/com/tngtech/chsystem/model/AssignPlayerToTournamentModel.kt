package com.tngtech.chsystem.model

import java.util.*

data class AssignPlayerToTournamentModel(
    val tournamentId: UUID,
    val playerId: UUID
)