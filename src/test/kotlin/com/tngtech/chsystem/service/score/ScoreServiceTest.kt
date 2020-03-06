package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.RankingScore
import com.tngtech.chsystem.dto.StatisticScore
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFailsWith


@ExtendWith(MockKExtension::class)
internal class ScoreServiceTest {

    @InjectMockKs
    lateinit var scoreService: ScoreService

    @MockK
    lateinit var rankingScoreService: RankingScoreService

    @MockK
    lateinit var statisticScoreService: StatisticScoreService

    private val tournament = TournamentEntity()
    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")

    @Test
    fun calculateScores() {

        val rankingScore1 = RankingScore(3, 0.0, 0.66, 0.33)
        val rankingScore2 = RankingScore(0, 3.0, 0.33, 0.66)
        val statisticScore1 = StatisticScore(1, 0, 0, 2, 1)
        val statisticScore2 = StatisticScore(0, 1, 0, 1, 2)

        every { rankingScoreService.calculatePlayerScores(tournament) } returns mapOf(
            player1 to rankingScore1,
            player2 to rankingScore2
        )
        every { statisticScoreService.calculateScores(tournament) } returns mapOf(
            player1 to statisticScore1,
            player2 to statisticScore2
        )

        val playerToScores = scoreService.calculateScores(tournament)

        assertThat(playerToScores[player1]?.rankingScore).isEqualTo(rankingScore1)
        assertThat(playerToScores[player2]?.rankingScore).isEqualTo(rankingScore2)
        assertThat(playerToScores[player1]?.statisticScore).isEqualTo(statisticScore1)
        assertThat(playerToScores[player2]?.statisticScore).isEqualTo(statisticScore2)
    }

    @Test
    fun `calculateScores throw exception if ranking and static scores are calculated for different players`() {

        val rankingScore1 = RankingScore(3, 0.0, 0.66, 0.33)
        val rankingScore2 = RankingScore(0, 3.0, 0.33, 0.66)
        val statisticScore1 = StatisticScore(1, 0, 0, 2, 1)

        every { rankingScoreService.calculatePlayerScores(tournament) } returns mapOf(
            player1 to rankingScore1,
            player2 to rankingScore2
        )
        every { statisticScoreService.calculateScores(tournament) } returns mapOf(
            player1 to statisticScore1
        )

        assertFailsWith<RuntimeException> {
            scoreService.calculateScores(tournament)
        }
    }
}