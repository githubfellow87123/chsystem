package com.tngtech.chsystem.dto

// This score is used to determine the ranking of players
data class RankingScore(
    val primaryScore: Int,
    val opponentAverageScore: Double,
    val gameWinPercentage: Double,
    val opponentAverageGameWinPercentage: Double
) : Comparable<RankingScore> {

    override fun compareTo(other: RankingScore) = when {
        primaryScore != other.primaryScore -> primaryScore.compareTo(other.primaryScore)
        opponentAverageScore != other.opponentAverageScore -> opponentAverageScore.compareTo(other.opponentAverageScore)
        gameWinPercentage != other.gameWinPercentage -> gameWinPercentage.compareTo(other.gameWinPercentage)
        else -> opponentAverageGameWinPercentage.compareTo(other.opponentAverageGameWinPercentage)
    }
}