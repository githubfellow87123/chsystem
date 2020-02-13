package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class OpponentAverageGameWinPercentageServiceUnitTest {

    private val opponentAverageGameWinPercentageService = OpponentAverageGameWinPercentageService()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")
    private val player4 = PlayerEntity(name = "Doro")
    private val tournament = TournamentEntity()


    @Test
    fun `calculateOpponentAverageGameWinPercentages 1 match played`() {

        val matches = setOf(createMatch(player1, player2))
        val playersToMatches = mapOf(player1 to matches, player2 to matches)
        val playerToGameWinPercentage = mapOf(player1 to 1.0, player2 to 0.0)

        val playerToOpponentAverageGameWinPercentage =
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )

        assertThat(playerToOpponentAverageGameWinPercentage[player1]).isEqualTo(0.0)
        assertThat(playerToOpponentAverageGameWinPercentage[player2]).isEqualTo(1.0)
    }

    @Test
    fun `calculateOpponentAverageGameWinPercentages bye`() {

        val matches = setOf(createMatch(player1, null))
        val playersToMatches = mapOf(player1 to matches)
        val playerToGameWinPercentage = mapOf(player1 to 1.0)

        val playerToOpponentAverageGameWinPercentage =
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )

        assertThat(playerToOpponentAverageGameWinPercentage[player1]).isEqualTo(0.0)
    }

    @Test
    fun `calculateOpponentAverageGameWinPercentages no matches played`() {

        val playersToMatches = mapOf(player1 to emptySet<PlayedMatch>())
        val playerToGameWinPercentage = mapOf(player1 to 1.0)

        val playerToOpponentAverageGameWinPercentage =
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )

        assertThat(playerToOpponentAverageGameWinPercentage[player1]).isEqualTo(0.0)
    }

    @Test
    fun `calculateOpponentAverageGameWinPercentages 4 players`() {

        val matchPlayer12 = createMatch(player1, player2)
        val matchPlayer14 = createMatch(player1, player4)
        val matchPlayer34 = createMatch(player3, player4)
        val matchPlayer23 = createMatch(player2, player3)

        val matchesPlayer1 = setOf(matchPlayer12, matchPlayer14)
        val matchesPlayer2 = setOf(matchPlayer12, matchPlayer23)
        val matchesPlayer3 = setOf(matchPlayer34, matchPlayer23)
        val matchesPlayer4 = setOf(matchPlayer14, matchPlayer34)
        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(
            player1 to matchesPlayer1,
            player2 to matchesPlayer2,
            player3 to matchesPlayer3,
            player4 to matchesPlayer4
        )

        val gameWinPercentagePlayer1 = 1.0
        val gameWinPercentagePlayer2 = 0.0
        val gameWinPercentagePlayer3 = 0.5
        val gameWinPercentagePlayer4 = 0.5

        val playerToGameWinPercentage = mapOf(
            player1 to gameWinPercentagePlayer1,
            player2 to gameWinPercentagePlayer2,
            player3 to gameWinPercentagePlayer3,
            player4 to gameWinPercentagePlayer4
        )

        val playerToOpponentAverageGameWinPercentage =
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )

        assertThat(playerToOpponentAverageGameWinPercentage[player1]).isEqualTo((gameWinPercentagePlayer2 + gameWinPercentagePlayer4) / 2)
        assertThat(playerToOpponentAverageGameWinPercentage[player2]).isEqualTo((gameWinPercentagePlayer1 + gameWinPercentagePlayer3) / 2)
        assertThat(playerToOpponentAverageGameWinPercentage[player3]).isEqualTo((gameWinPercentagePlayer2 + gameWinPercentagePlayer4) / 2)
        assertThat(playerToOpponentAverageGameWinPercentage[player4]).isEqualTo((gameWinPercentagePlayer1 + gameWinPercentagePlayer3) / 2)
    }

    private fun createMatch(
        player1: PlayerEntity,
        player2: PlayerEntity?
    ): PlayedMatch {
        return PlayedMatch(
            UUID.randomUUID(), tournament, 1, player1, player2,
            0, 0
        )
    }
}