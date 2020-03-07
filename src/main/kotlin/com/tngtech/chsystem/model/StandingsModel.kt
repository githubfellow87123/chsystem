package com.tngtech.chsystem.model

import com.tngtech.chsystem.dto.RankingScore
import java.time.LocalDateTime

data class StandingsModel(
    val playerName: String,
    val score: Int,
    val matchWins: Int,
    val matchLosses: Int,
    val matchDraws: Int,
    val opponentAverageScore: Double,
    val gameWins: Int,
    val gameLosses: Int,
    val gameWinPercentage: Double,
    val opponentAverageGameWinPercentage: Double,
    val latestMatchUpdate: LocalDateTime?
) : Comparable<StandingsModel> {

    override fun compareTo(other: StandingsModel): Int {
        val rankingsScore =
            RankingScore(score, opponentAverageScore, gameWinPercentage, opponentAverageGameWinPercentage)
        val rankingsScoreOther =
            RankingScore(
                other.score,
                other.opponentAverageScore,
                other.gameWinPercentage,
                other.opponentAverageGameWinPercentage
            )

        return when {
            rankingsScore != rankingsScoreOther -> rankingsScore.compareTo(rankingsScoreOther)
            latestMatchUpdate != other.latestMatchUpdate -> {
                when {
                    latestMatchUpdate == null -> 1
                    other.latestMatchUpdate == null -> -1
                    else -> other.latestMatchUpdate.compareTo(latestMatchUpdate)
                }
            }
            else -> other.playerName.compareTo(playerName)
        }
    }


}