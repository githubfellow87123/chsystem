package com.tngtech.chsystem.model

import com.tngtech.chsystem.entities.TournamentState
import java.time.LocalDate
import java.util.*

data class TournamentModel(
    val id: UUID? = null,
    val date: LocalDate = LocalDate.now(),
    var state: TournamentState? = null,
    var roundIndex: Int? = null
)