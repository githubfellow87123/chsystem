package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors
import kotlin.test.assertFailsWith

internal class GameWinPercentageServiceUnitTest {

    private val gameWinPercentageService = GameWinPercentageService()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val tournament = TournamentEntity()

    companion object {
        @JvmStatic
        fun onePlayerGameWinPercentages() = listOf(
            Arguments.of(listOf(2 to 0), 1.0),
            Arguments.of(listOf(2 to 1, 1 to 2), 0.5),
            Arguments.of(listOf(2 to 0, 2 to 0, 1 to 1, 0 to 2), 0.625),
            Arguments.of(listOf(0 to 0), 0.0),
            Arguments.of(emptyList<Pair<Int, Int>>(), 0.0)
        )
    }

    @ParameterizedTest
    @MethodSource("onePlayerGameWinPercentages")
    fun `calculateGameWinPercentages one player`(matchResults: List<Pair<Int, Int>>, gameWinPercentage: Double) {

        val matches = matchResults.stream()
            .map { matchResult -> createMatch(matchResult.first, matchResult.second) }
            .collect(Collectors.toSet())

        val playersToMatches = mapOf(player1 to matches)

        val playerToGameWinPercentage = gameWinPercentageService.calculateGameWinPercentages(playersToMatches)

        assertThat(playerToGameWinPercentage[player1]).isEqualTo(gameWinPercentage)
    }

    @Test
    fun `calculateGameWinPercentages multiple players`() {
        val matches = setOf(
            createMatch(2, 1),
            createMatch(0, 1),
            createMatch(1, 1),
            createMatch(0, 2)
        )

        val playersToMatches = mapOf(player1 to matches, player2 to matches)

        val playerToGameWinPercentage = gameWinPercentageService.calculateGameWinPercentages(playersToMatches)

        assertThat(playerToGameWinPercentage[player1]).isEqualTo(0.375)
        assertThat(playerToGameWinPercentage[player2]).isEqualTo(0.625)
    }

    @Test
    fun `calculateGameWinPercentages throws exception if match does not belong to player`() {
        val matches = setOf(
            PlayedMatch(
                UUID.randomUUID(), tournament, 1, player2, null,
                2, 0, LocalDateTime.now()
            )
        )

        val playersToMatches = mapOf(player1 to matches)

        assertFailsWith<RuntimeException> {
            gameWinPercentageService.calculateGameWinPercentages(playersToMatches)
        }
    }

    private fun createMatch(winsPlayer1: Int, winsPlayer2: Int): PlayedMatch {
        return PlayedMatch(
            UUID.randomUUID(), tournament, 1, player1, player2,
            winsPlayer1, winsPlayer2, LocalDateTime.now()
        )
    }
}