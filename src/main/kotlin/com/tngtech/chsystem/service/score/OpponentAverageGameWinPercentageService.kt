package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class OpponentAverageGameWinPercentageService {

    companion object {
        const val GAME_WIN_PERCENTAGE_BYE = 0.0
    }

    fun calculateOpponentAverageGameWinPercentages(
        playersToMatches: Map<PlayerEntity, Set<PlayedMatch>>,
        playerToGameWinPercentage: Map<PlayerEntity, Double>
    ): Map<PlayerEntity, Double> {

        val playerToOpponentAverageGameWinPercentage = HashMap<PlayerEntity, Double>()

        for ((player, matches) in playersToMatches) {
            playerToOpponentAverageGameWinPercentage[player] =
                calculateOpponentAverageGameWinPercentage(player, matches, playerToGameWinPercentage)
        }

        return playerToOpponentAverageGameWinPercentage
    }

    private fun calculateOpponentAverageGameWinPercentage(
        player: PlayerEntity,
        matches: Set<PlayedMatch>,
        playerToGameWinPercentage: Map<PlayerEntity, Double>
    ): Double {
        var sumOfOpponentGameWinPercentages = 0.0

        for (match in matches) {
            val opponent = when {
                match.player1 == player -> match.player2
                match.player2 == player -> match.player1
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }

            val opponentGameWinPercentage =
                if (opponent == null) GAME_WIN_PERCENTAGE_BYE else playerToGameWinPercentage.getValue(opponent)
            sumOfOpponentGameWinPercentages += opponentGameWinPercentage
        }

        return if (matches.isEmpty()) 0.0 else sumOfOpponentGameWinPercentages / matches.size
    }
}