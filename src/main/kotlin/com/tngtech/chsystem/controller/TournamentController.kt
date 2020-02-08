package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.MatchModel
import com.tngtech.chsystem.model.PlayerModel
import com.tngtech.chsystem.model.TournamentModel
import com.tngtech.chsystem.service.MatchmakingService
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.HashSet

@RestController
@RequestMapping("tournaments")
class TournamentController(
    private val tournamentRepository: TournamentRepository,
    private val matchmakingService: MatchmakingService
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

    @GetMapping
    fun listTournaments(): List<TournamentModel> {
        return tournamentRepository.findAll()
            .map { entity -> entity.toTournamentModel() }
            .sortedByDescending { tournament -> tournament.date }
    }

    @PutMapping("{tournamentId}/start")
    fun startTournament(@PathVariable tournamentId: UUID): TournamentModel {

        val tournament = tournamentRepository.findByIdOrNull(tournamentId)
            ?: throw TournamentDoesNotExistException(tournamentId)

        return when (tournament.state) {
            TournamentState.INITIALIZING -> {
                val matches = HashSet(tournament.matches)
                val matchesForNextRound =
                    matchmakingService.generateMatchesForRound(1, tournament.players, tournament.matches)
                matches.addAll(matchesForNextRound)

                val startedTournament = tournament.copy(
                    roundIndex = 1,
                    state = TournamentState.IN_PROGRESS,
                    matches = matches
                )

                tournamentRepository.save(startedTournament)
                startedTournament.toTournamentModel()
            }
            TournamentState.IN_PROGRESS -> tournament.toTournamentModel()
            TournamentState.DONE -> throw TournamentInWrongStateException("The tournament is already done and can not be started")
        }
    }

    // TODO add method to switch over to the next round in a tournament

    // TODO add method to finish a tournament

    // TODO add method to fetch a tournament

    private fun TournamentModel.toTournamentEntity(): TournamentEntity {
        return if (date == null) TournamentEntity() else TournamentEntity(date = date)
    }

    private fun TournamentEntity.toTournamentModel() = TournamentModel(id, date, state, roundIndex)

    private fun MatchEntity.toMatchModel() =
        MatchModel(id, roundIndex, player1.toPlayerModel(), player2.toPlayerModel())

    private fun PlayerEntity.toPlayerModel() = PlayerModel(id = id, name = name)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class TournamentDoesNotExistException(tournamentId: UUID) :
        RuntimeException("The tournament with id $tournamentId does not exist")

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class TournamentInWrongStateException(message: String) : RuntimeException(message)
}
