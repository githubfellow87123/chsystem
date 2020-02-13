package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.score.OpponentAverageScoreService.Companion.PRIMARY_SCORE_BYE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*

internal class OpponentAverageScoreServiceUnitTest {

    private val opponentAverageScoreService = OpponentAverageScoreService()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")
    private val player4 = PlayerEntity(name = "Doro")
    private val tournament = TournamentEntity()

    companion object {
        @JvmStatic
        fun twoPlayersOneMatchOpponentAverageScores() = listOf(
            Arguments.of(3, 0, 0.0, 3.0),
            Arguments.of(0, 3, 3.0, 0.0),
            Arguments.of(1, 1, 1.0, 1.0)
        )

        @JvmStatic
        fun fourPlayersTwoMatchesEachOpponentAverageScores() = listOf(
            Arguments.of(2, 2, 2, 2, 2.0, 2.0, 2.0, 2.0),
            Arguments.of(6, 0, 4, 1, 0.5, 5.0, 0.5, 5.0)
        )
    }

    @ParameterizedTest
    @MethodSource("twoPlayersOneMatchOpponentAverageScores")
    fun `calculateOpponentAverageScores 1 match played`(
        scorePlayer1: Int,
        scorePlayer2: Int,
        opponentAverageScorePlayer1: Double,
        opponentAverageScorePlayer2: Double
    ) {

        val playerToPrimaryScore = mapOf(player1 to scorePlayer1, player2 to scorePlayer2)
        val matches = setOf(createMatch(player1, player2))
        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to matches, player2 to matches)

        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)

        assertThat(playerToOpponentAverageScore[player1]).isEqualTo(opponentAverageScorePlayer1)
        assertThat(playerToOpponentAverageScore[player2]).isEqualTo(opponentAverageScorePlayer2)
    }

    @ParameterizedTest
    @MethodSource("fourPlayersTwoMatchesEachOpponentAverageScores")
    fun `calculateOpponentAverageScores 2 matches each player 4 players`(
        scorePlayer1: Int,
        scorePlayer2: Int,
        scorePlayer3: Int,
        scorePlayer4: Int,
        opponentAverageScorePlayer1: Double,
        opponentAverageScorePlayer2: Double,
        opponentAverageScorePlayer3: Double,
        opponentAverageScorePlayer4: Double
    ) {

        val playerToPrimaryScore = mapOf(
            player1 to scorePlayer1,
            player2 to scorePlayer2,
            player3 to scorePlayer3,
            player4 to scorePlayer4
        )


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

        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)

        assertThat(playerToOpponentAverageScore[player1]).isEqualTo(opponentAverageScorePlayer1)
        assertThat(playerToOpponentAverageScore[player2]).isEqualTo(opponentAverageScorePlayer2)
        assertThat(playerToOpponentAverageScore[player3]).isEqualTo(opponentAverageScorePlayer3)
        assertThat(playerToOpponentAverageScore[player4]).isEqualTo(opponentAverageScorePlayer4)
    }

    @Test
    fun `calculateOpponentAverageScores no matches played`() {

        val playerToPrimaryScore = mapOf(player1 to 0, player2 to 0)
        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to emptySet(), player2 to emptySet())

        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)

        assertThat(playerToOpponentAverageScore[player1]).isEqualTo(0.0)
        assertThat(playerToOpponentAverageScore[player2]).isEqualTo(0.0)
    }

    @Test
    fun `calculateOpponentAverageScores bye case`() {

        val playerToPrimaryScore = mapOf(player1 to 3)
        val matches = setOf(createMatch(player1, null))
        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to matches)

        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)

        assertThat(playerToOpponentAverageScore[player1]).isEqualTo(PRIMARY_SCORE_BYE.toDouble())
    }

    @Test
    fun `calculateOpponentAverageScores 1 player 3 matches`() {

        val playerToPrimaryScore = mapOf(player2 to 3, player3 to 6, player4 to 7)
        val matches = setOf(createMatch(player1, player2), createMatch(player1, player3), createMatch(player1, player4))
        val playersToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to matches)

        val playerToOpponentAverageScore =
            opponentAverageScoreService.calculateOpponentAverageScores(playersToMatches, playerToPrimaryScore)

        assertThat(playerToOpponentAverageScore[player1]).isEqualTo(16.0 / 3)
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