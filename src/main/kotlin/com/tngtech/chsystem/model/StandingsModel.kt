package com.tngtech.chsystem.model

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
)