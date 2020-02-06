package com.tngtech.chsystem.entities

import java.util.*
import javax.persistence.*

@Entity
data class MatchEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val roundIndex: Int,
    @ManyToOne
    @JoinColumn(name = "PLAYER_1_ID", nullable = false, updatable = false)
    val player1: PlayerEntity,
    @ManyToOne
    @JoinColumn(name = "PLAYER_2_ID", nullable = false, updatable = false)
    val player2: PlayerEntity
)