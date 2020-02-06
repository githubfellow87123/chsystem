package com.tngtech.chsystem.entities

import java.time.LocalDate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class TournamentEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),
    @Column(nullable = false)
    var state: TournamentState = TournamentState.INITIALIZING,
    @Column
    var roundIndex: Int
)