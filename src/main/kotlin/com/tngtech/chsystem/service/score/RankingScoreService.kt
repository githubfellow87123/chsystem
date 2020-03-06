package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.RankingScore
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import org.springframework.stereotype.Service

@Service
class RankingScoreService(
    private val primaryScoreService: PrimaryScoreService,
    private val opponentAverageScoreService: OpponentAverageScoreService,
    private val gameWinPercentageService: GameWinPercentageService,
    private val opponentAverageGameWinPercentageService: OpponentAverageGameWinPercentageService,
    private val matchService: MatchService
) {
    fun calculatePlayerScores(tournament: TournamentEntity): Map<PlayerEntity, RankingScore> {
        val playedMatches = matchService.getAllPlayedMatches(tournament.matches)
        val playerToMatches = matchService.mapPlayersToMatches(tournament.getPlayers(), playedMatches)
        return calculatePlayerScores(playerToMatches)
    }

    fun calculatePlayerScores(playersToMatches: Map<PlayerEntity, Set<PlayedMatch>>): Map<PlayerEntity, RankingScore> {
        val playerToPrimaryScore = primaryScoreService.calculatePrimaryScores(playersToMatches)
        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)
        val playerToGameWinPercentage = gameWinPercentageService.calculateGameWinPercentages(playersToMatches)
        val playerToOpponentAverageGameWinPercentage =
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )

        val playerToScore = HashMap<PlayerEntity, RankingScore>()

        for (player in playersToMatches.keys) {
            val score = RankingScore(
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