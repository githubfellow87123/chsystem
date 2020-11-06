package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.MatchEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface MatchRepository : CrudRepository<MatchEntity, UUID> {
    fun findAllByTournamentId(tournamentId: UUID): Iterable<MatchEntity>
}
