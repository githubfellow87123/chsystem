package com.tngtech.chsystem.model

import com.tngtech.chsystem.dto.RankingScore

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
    val opponentAverageGameWinPercentage: Double
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

        return rankingsScore.compareTo(rankingsScoreOther)

        // TODO sort by last played match when the scores are the same
        // return when {
        //     rankingsScore != rankingsScoreOther -> rankingsScore.compareTo(rankingsScoreOther)
        //     else -> score.compareTo(other.score)
        // }
    }


}