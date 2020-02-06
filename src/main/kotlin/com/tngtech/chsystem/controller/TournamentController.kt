package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.model.TournamentModel
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("tournaments")
class TournamentController(
    private val tournamentRepository: TournamentRepository
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun insertTournament(@RequestBody tournamentModel: TournamentModel): TournamentModel {

        val tournament = tournamentModel.toTournamentEntity()

        val tournamentEntity = tournamentRepository.save(tournament)
        logger.info { "Created new tournament $tournamentEntity" }

        return tournamentEntity.toTournamentModel()
    }

    private fun TournamentModel.toTournamentEntity(): TournamentEntity {
        return if (date == null) TournamentEntity() else TournamentEntity(date = date)
    }

    private fun TournamentEntity.toTournamentModel() = TournamentModel(id, date, state, roundIndex)
}