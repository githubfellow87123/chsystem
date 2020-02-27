package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.TournamentModel
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.matchmaking.MatchmakingCode
import com.tngtech.chsystem.service.matchmaking.MatchmakingService
import mu.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("tournaments")
class TournamentController(
    private val tournamentRepository: TournamentRepository,
    private val matchmakingService: MatchmakingService,
    private val matchService: MatchService
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
                val startedTournament = tournament.copy(
                    state = TournamentState.IN_PROGRESS
                )

                tournamentRepository.save(startedTournament)
                startedTournament.toTournamentModel()
            }
            TournamentState.IN_PROGRESS -> tournament.toTournamentModel()
            TournamentState.DONE -> throw TournamentInWrongStateException("The tournament is already done and can not be started")
        }
    }

    @PostMapping("{tournamentId}/nextRound")
    fun nextRoundOfTournament(@PathVariable tournamentId: UUID): TournamentModel {

        val tournament = tournamentRepository.findByIdOrNull(tournamentId)
            ?: throw TournamentDoesNotExistException(tournamentId)

        return when (tournament.state) {
            TournamentState.INITIALIZING -> throw TournamentInWrongStateException("The tournament is not started, start the tournament to continue to the next round")
            TournamentState.IN_PROGRESS -> {

                when (matchmakingService.generateMatchesForNextRound(tournament)) {
                    MatchmakingCode.SUCCESSFUL -> logger.info { "Generated matches for next round successfully, tournament $tournamentId" }
                    MatchmakingCode.MISSING_RESULTS_OF_CURRENT_ROUND -> throw UnableToGenerateMatchesException("Results of current round missing")
                    MatchmakingCode.NO_VALID_MATCHES_FOR_NEXT_ROUND_AVAILABLE -> throw UnableToGenerateMatchesException(
                        "It's not possible to generate matches for next round"
                    )
                }

                val startedTournament = tournament.copy(
                    roundIndex = tournament.roundIndex + 1
                )

                tournamentRepository.save(startedTournament)
                startedTournament.toTournamentModel()
            }
            TournamentState.DONE -> throw TournamentInWrongStateException("The tournament is already done, can't create an other round")
        }
    }

    @PutMapping("{tournamentId}/finish")
    fun finishTournament(@PathVariable tournamentId: UUID): TournamentModel {

        val tournament = tournamentRepository.findByIdOrNull(tournamentId)
            ?: throw TournamentDoesNotExistException(tournamentId)

        if (matchService.isResultMissing(tournament.matches)) {
            throw UnableToFinishTournamentException("Result of one or more matches are missing")
        }

        return when (tournament.state) {
            TournamentState.INITIALIZING -> throw TournamentInWrongStateException("The tournament needs to be started before finishing it")
            TournamentState.IN_PROGRESS -> {
                val finishedTournament = tournament.copy(
                    state = TournamentState.DONE
                )

                tournamentRepository.save(finishedTournament)
                finishedTournament.toTournamentModel()
            }
            TournamentState.DONE -> tournament.toTournamentModel()
        }
    }

    // TODO add method to fetch a tournament

    private fun TournamentModel.toTournamentEntity() = TournamentEntity(date = date)

    private fun TournamentEntity.toTournamentModel() = TournamentModel(id, date, state, roundIndex)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class TournamentDoesNotExistException(tournamentId: UUID) :
        RuntimeException("The tournament with id $tournamentId does not exist")

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class TournamentInWrongStateException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class UnableToGenerateMatchesException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class UnableToFinishTournamentException(message: String) : RuntimeException(message)
}
