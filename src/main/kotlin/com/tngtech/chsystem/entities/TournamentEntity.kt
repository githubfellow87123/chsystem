package com.tngtech.chsystem.entities

import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "TOURNAMENT")
data class TournamentEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val state: TournamentState = TournamentState.INITIALIZING,
    @Column
    val roundIndex: Int = 0,
    @ManyToMany
    @JoinTable(
        name = "TOURNAMENT_PLAYERS",
        joinColumns = [JoinColumn(name = "TOURNAMENT_ID", referencedColumnName = "ID")],
        inverseJoinColumns = [JoinColumn(name = "PLAYER_ID", referencedColumnName = "ID")]
    )
    var players: MutableSet<PlayerEntity> = mutableSetOf(),
    @OneToMany
    var matches: MutableSet<MatchEntity> = mutableSetOf()
)