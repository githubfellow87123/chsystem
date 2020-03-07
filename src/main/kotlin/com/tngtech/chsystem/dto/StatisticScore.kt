package com.tngtech.chsystem.dto

import java.time.LocalDateTime

// This score is just used to view statistics, ranks will not be determined by these values
data class StatisticScore(
    val matchWins: Int,
    val matchLosses: Int,
    val matchDraws: Int,
    val gameWins: Int,
    val gameLosses: Int,
    val latestMatchUpdate: LocalDateTime?
)