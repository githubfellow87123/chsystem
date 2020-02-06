package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.model.AssignPlayerToTournamentModel
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
        @RequestBody assignPlayerToTournamentModel: AssignPlayerToTournamentModel
    ) {
        if (!tournamentId.equals(assignPlayerToTournamentModel.tournamentId)) {
            throw TournamentMismatchException(
                "The tournamentId '$tournamentId' in the path variable does not match " +
                        "the id in the body ${assignPlayerToTournamentModel.tournamentId}"
            )
        }
        if (!playerId.equals(assignPlayerToTournamentModel.playerId)) {
            throw PlayerMismatchException(
                "The playerId '$playerId' in the path variable does not match " +
                        "the id in the body ${assignPlayerToTournamentModel.playerId}"
            )
        }

        val tournament = tournamentRepository.findByIdOrNull(tournamentId)
            ?: throw TournamentMismatchException("Tournament with id $tournamentId does not exist")

        val player = playerRepository.findByIdOrNull(playerId)
            ?: throw PlayerMismatchException("Player with id $playerId does not exist")

        tournament.players.add(player)
        tournamentRepository.save(tournament)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    class TournamentMismatchException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class PlayerMismatchException(message: String) : RuntimeException(message)

}