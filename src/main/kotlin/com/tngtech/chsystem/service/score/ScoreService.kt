package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.Score
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.springframework.stereotype.Service

@Service
class ScoreService(
    val rankingScoreService: RankingScoreService,
    val statisticScoreService: StatisticScoreService
) {
    fun calculateScores(tournament: TournamentEntity): Map<PlayerEntity, Score> {
        val playerToScore = HashMap<PlayerEntity, Score>()

        val playerToRankingScores = rankingScoreService.calculatePlayerScores(tournament)
        val playerToStatisticScores = statisticScoreService.calculateScores(tournament)

        if (!(playerToRankingScores.size == playerToStatisticScores.size &&
                    playerToRankingScores.keys.containsAll(playerToStatisticScores.keys))
        ) {
            throw RuntimeException("RankingScores were calculated for different players than StatisticScores")
        }

        for ((player, rankingScore) in playerToRankingScores) {
            playerToScore[player] = Score(rankingScore, playerToStatisticScores.getValue(player))
        }

        return playerToScore
    }
}