package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class ScoreServiceUnitTest {

    @MockK
    lateinit var primaryScoreService: PrimaryScoreService

    @MockK
    lateinit var opponentAverageScoreService: OpponentAverageScoreService

    @MockK
    lateinit var gameWinPercentageService: GameWinPercentageService

    @MockK
    lateinit var opponentAverageGameWinPercentageService: OpponentAverageGameWinPercentageService

    @InjectMockKs
    lateinit var scoreService: ScoreService

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val tournament = TournamentEntity()

    @Test
    fun calculatePlayerScores() {

        val matches = setOf(PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 2, 0))
        val playersToMatches = mapOf(player1 to matches, player2 to matches)

        val playerToPrimaryScore = mapOf(player1 to 3, player2 to 0)
        val playerToOpponentAverageScore = mapOf(player1 to 0.0, player2 to 3.0)
        val playerToGameWinPercentage = mapOf(player1 to 1.0, player2 to 0.0)
        val playerToOpponentAverageGameWinPercentage = mapOf(player1 to 1.0, player2 to 0.0)

        every { primaryScoreService.calculatePrimaryScores(playersToMatches) } returns playerToPrimaryScore
        every {
            opponentAverageScoreService.calculateOpponentAverageScores(
                playersToMatches,
                playerToPrimaryScore
            )
        } returns playerToOpponentAverageScore
        every { gameWinPercentageService.calculateGameWinPercentages(playersToMatches) } returns playerToGameWinPercentage
        every {
            opponentAverageGameWinPercentageService.calculateOpponentAverageGameWinPercentages(
                playersToMatches,
                playerToGameWinPercentage
            )
        } returns playerToOpponentAverageGameWinPercentage

        val playerToScore = scoreService.calculatePlayerScores(playersToMatches)

        assertThat(playerToScore.getValue(player1).primaryScore)
        assertThat(playerToScore.getValue(player1).opponentAverageScore)
        assertThat(playerToScore.getValue(player1).gameWinPercentage)
        assertThat(playerToScore.getValue(player1).opponentAverageGameWinPercentage)
        assertThat(playerToScore.getValue(player2).primaryScore)
        assertThat(playerToScore.getValue(player2).opponentAverageScore)
        assertThat(playerToScore.getValue(player2).gameWinPercentage)
        assertThat(playerToScore.getValue(player2).opponentAverageGameWinPercentage)
    }
}