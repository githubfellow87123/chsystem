package com.tngtech.chsystem.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.fail

internal class ScoreUnitTest {

    @ParameterizedTest
    @CsvSource("3,less,6", "6,equal,6", "9,greater,6")
    fun `compareTo primary score`(primaryScore1: Int, comparator: String, primaryScore2: Int) {
        val score1 = Score(primaryScore1, 0.0, 0.0, 0.0)
        val score2 = Score(primaryScore2, 0.0, 0.0, 0.0)

        compare(score1, score2, comparator)
    }

    @ParameterizedTest
    @CsvSource("3.0,less,6.0", "6.0,equal,6.0", "6.6,greater,6")
    fun `compareTo opponent average score`(
        opponentAverageScore1: Double,
        comparator: String,
        opponentAverageScore2: Double
    ) {
        val score1 = Score(0, opponentAverageScore1, 0.0, 0.0)
        val score2 = Score(0, opponentAverageScore2, 0.0, 0.0)

        compare(score1, score2, comparator)
    }

    @ParameterizedTest
    @CsvSource("0.33,less,0.5", "0.5,equal,0.5", "0.5111,greater,0.5")
    fun `compareTo game win percentage`(
        gameWinPercentage1: Double,
        comparator: String,
        gameWinPercentage2: Double
    ) {
        val score1 = Score(0, 0.0, gameWinPercentage1, 0.0)
        val score2 = Score(0, 0.0, gameWinPercentage2, 0.0)

        compare(score1, score2, comparator)
    }

    @ParameterizedTest
    @CsvSource("0.33,less,0.5", "0.5,equal,0.5", "0.5111,greater,0.5")
    fun `compareTo opponent average game win percentage`(
        opponentAverageGameWinPercentage1: Double,
        comparator: String,
        opponentAverageGameWinPercentage2: Double
    ) {
        val score1 = Score(0, 0.0, 0.0, opponentAverageGameWinPercentage1)
        val score2 = Score(0, 0.0, 0.0, opponentAverageGameWinPercentage2)

        compare(score1, score2, comparator)
    }

    @Test
    fun `compareTo primary score counts first`() {
        val score1 = Score(3, 0.0, 0.5, 0.5)
        val score2 = Score(0, 3.0, 0.6666, 0.6666)

        assertThat(score1).isGreaterThan(score2)
    }

    @Test
    fun `compareTo opponent average score counts second`() {
        val score1 = Score(3, 3.0, 0.5, 0.5)
        val score2 = Score(3, 0.0, 0.6666, 0.6666)

        assertThat(score1).isGreaterThan(score2)
    }

    @Test
    fun `compareTo game win percentage counts thrid`() {
        val score1 = Score(3, 3.0, 0.6666, 0.5)
        val score2 = Score(3, 3.0, 0.5, 0.6666)

        assertThat(score1).isGreaterThan(score2)
    }

    @Test
    fun `compareTo opponent average game win percentage counts fourth`() {
        val score1 = Score(3, 3.0, 0.6666, 0.6666)
        val score2 = Score(3, 3.0, 0.6666, 0.5)

        assertThat(score1).isGreaterThan(score2)
    }

    private fun compare(score1: Score, score2: Score, comparator: String) {
        when (comparator) {
            "less" -> assertThat(score1).isLessThan(score2)
            "equal" -> assertThat(score1).isEqualTo(score2)
            "greater" -> assertThat(score1).isGreaterThan(score2)
            else -> fail("Unknown comparator")
        }
    }
}