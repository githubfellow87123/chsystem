package com.tngtech.chsystem.entities

import java.io.Serializable
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class TournamentPlayerId(
    @Column
    val tournamentId: UUID,
    @Column
    val playerId: UUID
) : Serializable