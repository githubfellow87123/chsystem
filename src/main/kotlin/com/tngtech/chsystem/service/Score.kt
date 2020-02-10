package com.tngtech.chsystem.service

data class Score(
    val primaryScore: Int,
    val opponentAverageScore: Double,
    val gameWinPercentage: Double,
    val opponentAverageGameWinPercentage: Double
) : Comparable<Score> {

    override fun compareTo(other: Score) = when {
        primaryScore != other.primaryScore -> primaryScore.compareTo(other.primaryScore)
        opponentAverageScore != other.opponentAverageScore -> opponentAverageScore.compareTo(other.opponentAverageScore)
        gameWinPercentage != other.gameWinPercentage -> gameWinPercentage.compareTo(other.gameWinPercentage)
        else -> opponentAverageGameWinPercentage.compareTo(other.opponentAverageGameWinPercentage)
    }
}