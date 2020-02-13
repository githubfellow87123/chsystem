package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.score.PrimaryScoreService.Companion.SCORE_FOR_LOSS
import com.tngtech.chsystem.service.score.PrimaryScoreService.Companion.SCORE_FOR_TIE
import com.tngtech.chsystem.service.score.PrimaryScoreService.Companion.SCORE_FOR_WIN
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import kotlin.test.assertFailsWith

internal class PrimaryScoreServiceUnitTest {

    private val primaryScoreService = PrimaryScoreService()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")
    private val player4 = PlayerEntity(name = "Doro")
    private val tournament = TournamentEntity()

    companion object {
        @JvmStatic
        fun twoPlayerOneMatchScores() = listOf(
            Arguments.of(2, 0, SCORE_FOR_WIN, SCORE_FOR_LOSS),
            Arguments.of(2, 1, SCORE_FOR_WIN, SCORE_FOR_LOSS),
            Arguments.of(2, 2, SCORE_FOR_TIE, SCORE_FOR_TIE),
            Arguments.of(0, 0, SCORE_FOR_TIE, SCORE_FOR_TIE),
            Arguments.of(0, 1, SCORE_FOR_LOSS, SCORE_FOR_WIN),
            Arguments.of(0, 3, SCORE_FOR_LOSS, SCORE_FOR_WIN)
        )

        @JvmStatic
        fun twoPlayerTwoMatchesScores() = listOf(
            Arguments.of(2, 0, 2, 0, 2 * SCORE_FOR_WIN, 2 * SCORE_FOR_LOSS),
            Arguments.of(2, 1, 0, 2, SCORE_FOR_WIN + SCORE_FOR_LOSS, SCORE_FOR_LOSS + SCORE_FOR_WIN),
            Arguments.of(1, 2, 2, 1, SCORE_FOR_WIN + SCORE_FOR_LOSS, SCORE_FOR_LOSS + SCORE_FOR_WIN),
            Arguments.of(1, 2, 1, 2, 2 * SCORE_FOR_LOSS, 2 * SCORE_FOR_WIN),
            Arguments.of(0, 0, 0, 0, 2 * SCORE_FOR_TIE, 2 * SCORE_FOR_TIE),
            Arguments.of(2, 0, 1, 1, SCORE_FOR_WIN + SCORE_FOR_TIE, SCORE_FOR_LOSS + SCORE_FOR_TIE),
            Arguments.of(0, 0, 0, 1, SCORE_FOR_LOSS + SCORE_FOR_TIE, SCORE_FOR_WIN + SCORE_FOR_TIE)
        )
    }

    @ParameterizedTest
    @MethodSource("twoPlayerOneMatchScores")
    fun `calculatePrimaryScores 2 player 1 match`(
        winsPlayer1: Int,
        winsPlayer2: Int,
        scorePlayer1: Int,
        scorePlayer2: Int
    ) {
        val matches = setOf(createMatch(player1, player2, winsPlayer1, winsPlayer2))
        val playersToMatches = mapOf(player1 to matches, player2 to matches)

        val playerToPrimaryScore = primaryScoreService.calculatePrimaryScores(playersToMatches)

        assertThat(playerToPrimaryScore[player1]).isEqualTo(scorePlayer1)
        assertThat(playerToPrimaryScore[player2]).isEqualTo(scorePlayer2)
    }

    @ParameterizedTest
    @MethodSource("twoPlayerTwoMatchesScores")
    fun `calculatePrimaryScores 2 player 2 matches`(
        winsPlayer1Match1: Int,
        winsPlayer2Match1: Int,
        winsPlayer1Match2: Int,
        winsPlayer2Match2: Int,
        scorePlayer1: Int,
        scorePlayer2: Int
    ) {
        val match1 = createMatch(player1, player2, winsPlayer1Match1, winsPlayer2Match1)
        val match2 = createMatch(player1, player2, winsPlayer1Match2, winsPlayer2Match2)
        val matches = setOf(match1, match2)
        val playersToMatches = mapOf(player1 to matches, player2 to matches)

        val playerToPrimaryScore = primaryScoreService.calculatePrimaryScores(playersToMatches)

        assertThat(playerToPrimaryScore[player1]).isEqualTo(scorePlayer1)
        assertThat(playerToPrimaryScore[player2]).isEqualTo(scorePlayer2)
    }

    @Test
    fun `calculatePrimaryScores if no matches were played all players have 0 points`() {
        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to emptySet(), player2 to emptySet())

        val playerToPrimaryScore = primaryScoreService.calculatePrimaryScores(playersToMatches)

        assertThat(playerToPrimaryScore[player1]).isEqualTo(0)
        assertThat(playerToPrimaryScore[player2]).isEqualTo(0)
    }

    @Test
    fun `calculatePrimaryScores throws exception is match is mapped to the wrong player`() {

        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> =
            mapOf(player1 to setOf(createMatch(player2, player3, 2, 0)))

        assertFailsWith<RuntimeException> { primaryScoreService.calculatePrimaryScores(playersToMatches) }
    }

    @Test
    fun `calculatePrimaryScores 4 players 3 matches each`() {
        val matchPlayer12 = createMatch(player1, player2, 2, 0)
        val matchPlayer34 = createMatch(player3, player4, 2, 1)
        val matchPlayer13 = createMatch(player1, player3, 2, 1)
        val matchPlayer24 = createMatch(player2, player4, 0, 2)
        val matchPlayer14 = createMatch(player1, player4, 1, 1)
        val matchPlayer23 = createMatch(player2, player3, 2, 0)


        val matchesPlayer1 = setOf(matchPlayer12, matchPlayer13, matchPlayer14)
        val matchesPlayer2 = setOf(matchPlayer12, matchPlayer24, matchPlayer23)
        val matchesPlayer3 = setOf(matchPlayer34, matchPlayer13, matchPlayer23)
        val matchesPlayer4 = setOf(matchPlayer34, matchPlayer24, matchPlayer14)
        val playersToMatches = mapOf(
            player1 to matchesPlayer1,
            player2 to matchesPlayer2,
            player3 to matchesPlayer3,
            player4 to matchesPlayer4
        )

        val playerToPrimaryScore = primaryScoreService.calculatePrimaryScores(playersToMatches)

        assertThat(playerToPrimaryScore[player1]).isEqualTo(2 * SCORE_FOR_WIN + SCORE_FOR_TIE)
        assertThat(playerToPrimaryScore[player2]).isEqualTo(SCORE_FOR_WIN + 2 * SCORE_FOR_LOSS)
        assertThat(playerToPrimaryScore[player3]).isEqualTo(SCORE_FOR_WIN + 2 * SCORE_FOR_LOSS)
        assertThat(playerToPrimaryScore[player4]).isEqualTo(SCORE_FOR_LOSS + SCORE_FOR_WIN + SCORE_FOR_TIE)
    }

    private fun createMatch(
        player1: PlayerEntity,
        player2: PlayerEntity,
        winsPlayer1: Int,
        winsPlayer2: Int
    ): PlayedMatch {
        return PlayedMatch(
            UUID.randomUUID(), tournament, 1, player1, player2,
            winsPlayer1, winsPlayer2
        )
    }
}