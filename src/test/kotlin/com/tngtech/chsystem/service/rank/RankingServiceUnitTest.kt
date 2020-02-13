package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.Score
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.score.ScoreService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class RankingServiceUnitTest {

    @MockK
    lateinit var scoreService: ScoreService

    @MockK
    lateinit var playerMatchesService: PlayerMatchesService

    private val random = Random()

    private lateinit var rankingService: RankingService

    private val tournament = TournamentEntity()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")
    private val player4 = PlayerEntity(name = "Doro")

    @BeforeEach
    fun setUp() {
        rankingService = RankingService(scoreService, playerMatchesService, random)
    }

    @Test
    fun rankPlayers() {
        val matchPlayer12 = PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 0, 2)
        val matchPlayer34 = PlayedMatch(UUID.randomUUID(), tournament, 1, player3, player4, 1, 2)

        val players = setOf(player1, player2, player3, player4)
        val alreadyPlayedMatches = setOf(matchPlayer12, matchPlayer34)

        val playerToMatches = mapOf(
            player1 to setOf(matchPlayer12),
            player2 to setOf(matchPlayer12),
            player3 to setOf(matchPlayer34),
            player4 to setOf(matchPlayer34)
        )
        every { playerMatchesService.mapPlayersToMatches(players, alreadyPlayedMatches) } returns playerToMatches
        val playerToScores = mapOf(
            player1 to Score(0, 3.0, 0.0, 1.0),
            player2 to Score(3, 0.0, 1.0, 1.0),
            player3 to Score(0, 3.0, 1.0 / 3, 2.0 / 3),
            player4 to Score(3, 0.0, 2.0 / 3, 1.0 / 3)
        )
        every { scoreService.calculatePlayerScores(playerToMatches) } returns playerToScores

        val playersOrderedByRank = rankingService.rankPlayers(players, alreadyPlayedMatches)

        assertThat(playerToScores.getValue(player2)).isGreaterThan(playerToScores.getValue(player4))
        assertThat(playerToScores.getValue(player4)).isGreaterThan(playerToScores.getValue(player3))
        assertThat(playerToScores.getValue(player3)).isGreaterThan(playerToScores.getValue(player1))
        assertThat(playersOrderedByRank[0]).isEqualTo(player2)
        assertThat(playersOrderedByRank[1]).isEqualTo(player4)
        assertThat(playersOrderedByRank[2]).isEqualTo(player3)
        assertThat(playersOrderedByRank[3]).isEqualTo(player1)
    }

    @Test
    fun `rankPlayers players with same score are shuffled sanity check`() {
        val players = setOf(player1, player2)
        val alreadyPlayedMatches: Set<PlayedMatch> = emptySet()

        val playerToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to emptySet(), player2 to emptySet())
        every { playerMatchesService.mapPlayersToMatches(players, alreadyPlayedMatches) } returns playerToMatches
        val scorePlayer13 = Score(0, 0.0, 0.0, 0.0)
        val scorePlayer24 = Score(3, 0.0, 0.0, 0.0)
        val playerToScores = mapOf(
            player1 to scorePlayer13,
            player2 to scorePlayer24,
            player3 to scorePlayer13,
            player4 to scorePlayer24
        )
        every { scoreService.calculatePlayerScores(playerToMatches) } returns playerToScores

        assertThat(scorePlayer24).isGreaterThan(scorePlayer13)

        val rankingService1 = RankingService(scoreService, playerMatchesService, Random(1L))
        val rankingService2 = RankingService(scoreService, playerMatchesService, Random(123743L))

        val playersOrderedByRank1 = rankingService1.rankPlayers(players, alreadyPlayedMatches)
        assertThat(playersOrderedByRank1[0]).isEqualTo(player2)
        assertThat(playersOrderedByRank1[1]).isEqualTo(player4)
        assertThat(playersOrderedByRank1[2]).isEqualTo(player3)
        assertThat(playersOrderedByRank1[3]).isEqualTo(player1)

        val playersOrderedByRank2 = rankingService2.rankPlayers(players, alreadyPlayedMatches)
        assertThat(playersOrderedByRank2[0]).isEqualTo(player4)
        assertThat(playersOrderedByRank2[1]).isEqualTo(player2)
        assertThat(playersOrderedByRank2[2]).isEqualTo(player1)
        assertThat(playersOrderedByRank2[3]).isEqualTo(player3)
    }
}