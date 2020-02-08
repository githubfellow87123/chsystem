package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.PlayerToTournamentModel
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("tournaments/{tournamentId}/players")
class TournamentPlayerController(
    private val tournamentRepository: TournamentRepository,
    private val playerRepository: PlayerRepository
) {

    @PutMapping("{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun assignPlayerToTournament(
        @PathVariable tournamentId: UUID,
        @PathVariable playerId: UUID,
        @RequestBody playerToTournamentModel: PlayerToTournamentModel
    ) {
        checkIfIdsMatch(tournamentId, playerId, playerToTournamentModel)
        val tournament = findTournament(tournamentId)
        checkTournamentState(tournament)

        val player = findPlayer(playerId)

        val players = tournament.players.plus(player)
        val updatedTournament = tournament.copy(players = players)
        tournamentRepository.save(updatedTournament)
    }

    @DeleteMapping("{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removePlayerFromTournament(
        @PathVariable tournamentId: UUID,
        @PathVariable playerId: UUID,
        @RequestBody playerToTournamentModel: PlayerToTournamentModel
    ) {
        checkIfIdsMatch(tournamentId, playerId, playerToTournamentModel)
        val tournament = findTournament(tournamentId)
        checkTournamentState(tournament)

        val player = findPlayer(playerId)

        if (tournament.players.contains(player)) {
            val players = tournament.players.minus(player)
            val updatedTournament = tournament.copy(players = players)
            tournamentRepository.save(updatedTournament)
        } else {
            throw PlayerNotAssignedToTournamentException("Player with id $playerId is not assigned to tournament with id $tournamentId")
        }
    }

    private fun checkTournamentState(tournament: TournamentEntity) {
        if (tournament.state != TournamentState.INITIALIZING) {
            throw TournamentAlreadyStartedException("Can't change participating players when tournament is already started")
        }
    }

    private fun findPlayer(playerId: UUID) = (playerRepository.findByIdOrNull(playerId)
        ?: throw PlayerMismatchException("Player with id $playerId does not exist"))

    private fun findTournament(tournamentId: UUID) = (tournamentRepository.findByIdOrNull(tournamentId)
        ?: throw TournamentMismatchException("Tournament with id $tournamentId does not exist"))

    private fun checkIfIdsMatch(
        tournamentId: UUID,
        playerId: UUID,
        playerToTournamentModel: PlayerToTournamentModel
    ) {
        if (tournamentId != playerToTournamentModel.tournamentId) {
            throw TournamentMismatchException(
                "The tournamentId '$tournamentId' in the path variable does not match " +
                        "the id in the body ${playerToTournamentModel.tournamentId}"
            )
        }
        if (playerId != playerToTournamentModel.playerId) {
            throw PlayerMismatchException(
                "The playerId '$playerId' in the path variable does not match " +
                        "the id in the body ${playerToTournamentModel.playerId}"
            )
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    class TournamentMismatchException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class PlayerMismatchException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class PlayerNotAssignedToTournamentException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class TournamentAlreadyStartedException(message: String) : RuntimeException(message)

}