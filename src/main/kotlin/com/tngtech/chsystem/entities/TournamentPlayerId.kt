package com.tngtech.chsystem.entities

import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class TournamentPlayerId(
    @Column
    val tournamentId: UUID,
    @Column
    val playerId: UUID
) : Serializable