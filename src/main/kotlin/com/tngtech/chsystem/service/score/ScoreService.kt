package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.Score
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class ScoreService(
    private val primaryScoreService: PrimaryScoreService,
    private val opponentAverageScoreService: OpponentAverageScoreService,
    private val gameWinPercentageService: GameWinPercentageService,
    private val opponentAverageGameWinPercentageService: OpponentAverageGameWinPercentageService
) {
    fun calculatePlayerScores(playersToMatches: Map<PlayerEntity, Set<PlayedMatch>>): Map<PlayerEntity, Score> {
        val playerToPrimaryScore = primaryScoreService.calculatePrimaryScores(playersToMatches)
        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)
        val playerToGameWinPercentage = gameWinPercentageService.calculateGameWinPercentages(playersToMatches)
        val playerToOpponentAverageGameWinPercentage =
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )

        val playerToScore = HashMap<PlayerEntity, Score>()

        for (player in playersToMatches.keys) {
            val score = Score(
                playerToPrimaryScore.getValue(player),
                playerToOpponentAverageScore.getValue(player),
                playerToGameWinPercentage.getValue(player),
                playerToOpponentAverageGameWinPercentage.getValue(player)
            )
            playerToScore[player] = score
        }

        return playerToScore
    }
}