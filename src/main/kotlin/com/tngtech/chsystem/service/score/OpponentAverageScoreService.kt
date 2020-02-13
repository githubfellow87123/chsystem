package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class OpponentAverageScoreService {

    companion object {
        const val PRIMARY_SCORE_BYE = 0
    }

    fun calculateOpponentAverageScores(
        playersToMatches: Map<PlayerEntity, Set<PlayedMatch>>,
        playerToPrimaryScore: Map<PlayerEntity, Int>
    ): Map<PlayerEntity, Double> {

        val playerToOpponentAverageScore = HashMap<PlayerEntity, Double>()

        for ((player, matches) in playersToMatches) {
            playerToOpponentAverageScore[player] = calculateOpponentAverageScore(player, matches, playerToPrimaryScore)
        }

        return playerToOpponentAverageScore
    }

    private fun calculateOpponentAverageScore(
        player: PlayerEntity,
        matches: Set<PlayedMatch>,
        playerToPrimaryScore: Map<PlayerEntity, Int>
    ): Double {
        if (matches.isEmpty()) {
            return 0.0
        }

        var sumOfOpponentScores = 0

        for (match in matches) {
            val opponent = when {
                match.player1 == player -> match.player2
                match.player2 == player -> match.player1
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }

            val opponentScore = if (opponent == null) PRIMARY_SCORE_BYE else playerToPrimaryScore.getValue(opponent)
            sumOfOpponentScores += opponentScore
        }

        return sumOfOpponentScores.toDouble() / matches.size
    }
}