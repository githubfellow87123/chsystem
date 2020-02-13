package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class PrimaryScoreService {
    companion object {
        const val SCORE_FOR_WIN = 3
        const val SCORE_FOR_TIE = 1
        const val SCORE_FOR_LOSS = 0
    }

    fun calculatePrimaryScores(playersToMatches: Map<PlayerEntity, Set<PlayedMatch>>): Map<PlayerEntity, Int> {

        val playerToPrimaryScore = HashMap<PlayerEntity, Int>()

        for ((player, matches) in playersToMatches) {
            playerToPrimaryScore[player] = calculatePrimaryScore(player, matches)
        }

        return playerToPrimaryScore
    }

    private fun calculatePrimaryScore(player: PlayerEntity, matches: Set<PlayedMatch>): Int {

        var primaryScore = 0

        for (match in matches) {
            primaryScore += when {
                match.player1 == player -> calculatePrimaryScorePlayer1(
                    match.winsPlayer1,
                    match.winsPlayer2
                )
                match.player2 == player -> calculatePrimaryScorePlayer1(
                    match.winsPlayer2,
                    match.winsPlayer1
                )
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return primaryScore
    }

    private fun calculatePrimaryScorePlayer1(winsPlayer1: Int, winsPlayer2: Int): Int {
        return when {
            winsPlayer1 < winsPlayer2 -> SCORE_FOR_LOSS
            winsPlayer1 > winsPlayer2 -> SCORE_FOR_WIN
            else -> SCORE_FOR_TIE
        }
    }
}