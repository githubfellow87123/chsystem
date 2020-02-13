package com.tngtech.chsystem.entities

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "MATCH")
data class MatchEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    @ManyToOne
    val tournament: TournamentEntity,
    @Column(nullable = false)
    val roundIndex: Int,
    @ManyToOne
    @JoinColumn(name = "PLAYER_1_ID", nullable = false, updatable = false)
    val player1: PlayerEntity,
    @ManyToOne
    @JoinColumn(name = "PLAYER_2_ID", nullable = true, updatable = false)
    val player2: PlayerEntity?,
    @Column(nullable = true)
    val winsPlayer1: Int? = null,
    @Column(nullable = true)
    val winsPlayer2: Int? = null
)