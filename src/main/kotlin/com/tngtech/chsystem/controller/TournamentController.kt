package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.StandingsModel
import com.tngtech.chsystem.model.TournamentModel
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.matchmaking.MatchmakingCode
import com.tngtech.chsystem.service.matchmaking.MatchmakingService
import com.tngtech.chsystem.service.rank.RankingService
import com.tngtech.chsystem.service.score.ScoreService
import mu.KotlinLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.collections.ArrayList

@RestController
@RequestMapping("tournaments")
class TournamentController(
    private val tournamentRepository: TournamentRepository,
    private val matchmakingService: MatchmakingService,
    private val matchService: MatchService,
    private val rankingService: RankingService,
    private val scoreService: ScoreService,
    private val random: Random
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

    @GetMapping("{id}")
    fun findTournamentById(@PathVariable id: UUID): TournamentModel {

        val tournament = tournamentRepository.findById(id)

        if (tournament.isEmpty) {
            throw TournamentDoesNotExistException(id)
        }

        return tournament.get().toTournamentModel()
    }

    @DeleteMapping("{id}")
    fun deleteTournament(@PathVariable id: UUID) {
        try {
            tournamentRepository.deleteById(id)
        } catch (e: EmptyResultDataAccessException) {
            throw TournamentDoesNotExistException(id)
        }
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

                for ((i, player) in tournament.getPlayers().shuffled(random).withIndex()) {
                    startedTournament.setSeatingOrderOfPlayer(player, i + 1)
                }

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
                val finishedTournament = generateFinishedTournament(tournament)
                tournamentRepository.save(finishedTournament)
                finishedTournament.toTournamentModel()
            }
            TournamentState.DONE -> tournament.toTournamentModel()
        }
    }

    private fun generateFinishedTournament(tournament: TournamentEntity): TournamentEntity {
        val finishedTournament = tournament.copy(
            state = TournamentState.DONE
        )

        val rankedPlayers =
            rankingService.rankPlayers(tournament) ?: throw UnableToFinishTournamentException("Unable to rank players")
        for (i in rankedPlayers.indices) {
            if (!finishedTournament.setRankOfPlayer(rankedPlayers[i], i + 1)) {
                logger.warn("Player ${rankedPlayers[i].id} not found in tournament ${tournament.id}")
            }
        }
        return finishedTournament
    }

    @GetMapping("{tournamentId}/standings")
    fun getStandings(@PathVariable tournamentId: UUID): List<StandingsModel> {

        val tournament = tournamentRepository.findByIdOrNull(tournamentId)
            ?: throw TournamentDoesNotExistException(tournamentId)
        val playerToScores = scoreService.calculateScores(tournament)

        val standings = ArrayList<StandingsModel>()

        for ((player, score) in playerToScores) {
            val standing = StandingsModel(
                playerName = player.name,
                score = score.rankingScore.primaryScore,
                matchWins = score.statisticScore.matchWins,
                matchLosses = score.statisticScore.matchLosses,
                matchDraws = score.statisticScore.matchDraws,
                opponentAverageScore = score.rankingScore.opponentAverageScore,
                gameWins = score.statisticScore.gameWins,
                gameLosses = score.statisticScore.gameLosses,
                gameWinPercentage = score.rankingScore.gameWinPercentage,
                opponentAverageGameWinPercentage = score.rankingScore.opponentAverageGameWinPercentage,
                latestMatchUpdate = score.statisticScore.latestMatchUpdate
            )
            standings.add(standing)
        }

        if (tournament.state == TournamentState.DONE) {
            standings.sortBy { standing -> tournament.getRankOfPlayer(standing.playerName) }
        } else {
            standings.sortByDescending { it }
        }

        return standings
    }

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
