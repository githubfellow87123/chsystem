package com.tngtech.chsystem.entities

import java.time.LocalDate
import java.util.*
import javax.persistence.*

@Entity
data class TournamentEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var state: TournamentState = TournamentState.INITIALIZING,
    @Column
    var roundIndex: Int = 0
)