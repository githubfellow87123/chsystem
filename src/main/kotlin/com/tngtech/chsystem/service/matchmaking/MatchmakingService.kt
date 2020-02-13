package com.tngtech.chsystem.service.matchmaking

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.service.rank.RankingService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class MatchmakingService(
    private val rankingService: RankingService
) {

    // finish matchmaking implementation
    fun generateMatchesForRound(
        roundIndex: Int,
        players: Set<PlayerEntity>,
        alreadyPlayedMatches: Set<MatchEntity>
    ): Set<MatchEntity> {

        val playedMatches: Set<PlayedMatch> = alreadyPlayedMatches.stream()
            .map { matchEntity -> matchEntity.toPlayedMatch() }
            .collect(Collectors.toSet())

        // TODO use ranked players for creation of new matches
        val rankedPlayers = rankingService.rankPlayers(players, playedMatches)

        return emptySet()
    }
    
    // TODO use and test this function
    fun playedVsAnother(
        player1: PlayerEntity,
        player2: PlayerEntity?,
        alreadyPlayedMatches: Set<MatchEntity>
    ): Boolean {

        return alreadyPlayedMatches.stream()
            .anyMatch { match ->
                match.player1 == player1 && match.player2 == player2
                        || match.player1 == player2 && match.player2 == player1
            }
    }

    private fun MatchEntity.toPlayedMatch(): PlayedMatch {
        if (winsPlayer1 == null || winsPlayer2 == null) {
            throw RuntimeException("Match $this is was not played, results are missing")
        }

        return PlayedMatch(
            id,
            tournament,
            roundIndex,
            player1,
            player2,
            winsPlayer1!!,
            winsPlayer2!!
        )
    }
}