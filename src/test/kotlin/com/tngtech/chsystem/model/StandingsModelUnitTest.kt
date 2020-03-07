package com.tngtech.chsystem.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class StandingsModelUnitTest {

    @Test
    fun `compare greater score implies greater standing`() {
        val standingsModel1 = createStandingsModel(score = 3)
        val standingsModel2 = createStandingsModel(score = 0)

        assertThat(standingsModel1.compareTo(standingsModel2)).isGreaterThan(0)
    }

    @Test
    fun `compare if score and latestMatchUpdate are equal compare by name`() {
        val standingsModel1 = createStandingsModel(name = "Alex")
        val standingsModel2 = createStandingsModel(name = "Bert")

        assertThat(standingsModel1.compareTo(standingsModel2)).isGreaterThan(0)
    }

    @Test
    fun `compare if score is equal compare by latestMatchUpdate, earlier datetime greater than later datetime`() {
        val standingsModel1 = createStandingsModel(latestMatchUpdate = LocalDateTime.MIN)
        val standingsModel2 = createStandingsModel(latestMatchUpdate = LocalDateTime.MAX)

        assertThat(standingsModel1.compareTo(standingsModel2)).isGreaterThan(0)
    }

    @Test
    fun `compare latestMatchUpdate, null is the greatest datetime`() {
        val standingsModel1 = createStandingsModel(latestMatchUpdate = null)
        val standingsModel2 = createStandingsModel(latestMatchUpdate = LocalDateTime.MAX)

        assertThat(standingsModel1.compareTo(standingsModel2)).isGreaterThan(0)
        assertThat(standingsModel2.compareTo(standingsModel1)).isLessThan(0)
    }

    private fun createStandingsModel(
        score: Int = 0,
        name: String = "Alex",
        latestMatchUpdate: LocalDateTime? = null
    ): StandingsModel {
        return StandingsModel(
            playerName = name,
            score = score,
            matchWins = 0,
            matchLosses = 0,
            matchDraws = 0,
            opponentAverageScore = 0.0,
            gameWins = 0,
            gameLosses = 0,
            gameWinPercentage = 0.0,
            opponentAverageGameWinPercentage = 0.0,
            latestMatchUpdate = latestMatchUpdate
        )
    }
}