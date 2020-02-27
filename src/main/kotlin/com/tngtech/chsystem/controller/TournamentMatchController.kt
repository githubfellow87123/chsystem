package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.MatchRepository
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.MatchResultModel
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("tournaments/{tournamentId}/matches")
class TournamentMatchController(
    val matchRepository: MatchRepository
) {

    @PutMapping("{matchId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun enterMatchResult(
        @PathVariable tournamentId: UUID,
        @PathVariable matchId: UUID,
        @RequestBody matchResultModel: MatchResultModel
    ) {
        if (matchId != matchResultModel.id) {
            throw MatchMismatchException(
                "The matchId '$matchId' in the path variable does not match " +
                        "the id in the body ${matchResultModel.id}"
            )
        }

        val match = matchRepository.findByIdOrNull(matchId)
            ?: throw MatchMismatchException("The match with id '$matchId' does not exist")

        if (match.tournament.id != tournamentId) {
            throw TournamentMismatchException("The match with id '$matchId' does not belong to tournament '$tournamentId'")
        }

        if (match.tournament.state != TournamentState.IN_PROGRESS) {
            throw TournamentInWrongStateException("The tournament is not in progress")
        }

        if (match.tournament.roundIndex != match.roundIndex) {
            throw TournamentInWrongStateException(
                "Round of the tournament is '${match.tournament.roundIndex}'. " +
                        "Can't enter result of match that belongs to round '${match.roundIndex}'"
            )
        }

        val matchWithEnteredResult = match.copy(
            winsPlayer1 = matchResultModel.winsPlayer1,
            winsPlayer2 = matchResultModel.winsPlayer2
        )

        matchRepository.save(matchWithEnteredResult)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    class MatchMismatchException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class TournamentMismatchException(message: String) : RuntimeException(message)

    @ResponseStatus(HttpStatus.CONFLICT)
    class TournamentInWrongStateException(message: String) : RuntimeException(message)
}