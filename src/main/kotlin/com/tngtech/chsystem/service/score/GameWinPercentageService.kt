package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class GameWinPercentageService {
    fun calculateGameWinPercentages(playersToMatches: Map<PlayerEntity, Set<PlayedMatch>>): Map<PlayerEntity, Double> {

        val playerToGameWinPercentage = HashMap<PlayerEntity, Double>()

        for ((player, matches) in playersToMatches) {
            playerToGameWinPercentage[player] = calculateGameWinPercentage(player, matches)
        }

        return playerToGameWinPercentage
    }

    private fun calculateGameWinPercentage(player: PlayerEntity, matches: Set<PlayedMatch>): Double {
        if (matches.isEmpty()) {
            return 0.0
        }

        var wins = 0
        var losses = 0

        for (match in matches) {
            when {
                match.player1 == player -> {
                    wins += match.winsPlayer1
                    losses += match.winsPlayer2
                }
                match.player2 == player -> {
                    wins += match.winsPlayer2
                    losses += match.winsPlayer1
                }
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }

        return if (wins + losses == 0) 0.0 else wins.toDouble() / (wins + losses)
    }
}