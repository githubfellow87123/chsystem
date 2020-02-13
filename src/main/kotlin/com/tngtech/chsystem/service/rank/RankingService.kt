package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.service.score.ScoreService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class RankingService(
    private val scoreService: ScoreService
) {

    fun rankPlayers(players: Set<PlayerEntity>, alreadyPlayedMatches: Set<PlayedMatch>): List<PlayerEntity> {

        // TODO finish implementation
        val playerToMatches = mapPlayersToMatches(players, alreadyPlayedMatches)
        val playerToScore = scoreService.calculatePlayerScores(playerToMatches)

        return players.toList()
    }


    // TODO test this function
    internal fun mapPlayersToMatches(
        players: Set<PlayerEntity>,
        alreadyPlayedMatches: Set<PlayedMatch>
    ): Map<PlayerEntity, Set<PlayedMatch>> {

        val playerToMatches = HashMap<PlayerEntity, Set<PlayedMatch>>()

        for (player in players) {
            val matchesOfPlayer = alreadyPlayedMatches.stream()
                .filter { match ->
                    match.player1 == player || match.player2 == player
                }
                .collect(Collectors.toSet())
            playerToMatches[player] = matchesOfPlayer
        }

        return playerToMatches
    }
}