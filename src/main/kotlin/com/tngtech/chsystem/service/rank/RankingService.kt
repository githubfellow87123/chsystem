package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.service.score.ScoreService
import org.springframework.stereotype.Service

@Service
class RankingService(
    private val scoreService: ScoreService,
    private val playerMatchesService: PlayerMatchesService
) {

    fun rankPlayers(players: Set<PlayerEntity>, alreadyPlayedMatches: Set<PlayedMatch>): List<PlayerEntity> {

        val playerToMatches = playerMatchesService.mapPlayersToMatches(players, alreadyPlayedMatches)
        val playerToScore = scoreService.calculatePlayerScores(playerToMatches)

        return players.toList()
            .sortedByDescending { playerToScore.getValue(it) }
    }


}