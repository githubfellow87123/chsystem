package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.RankingScore
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.score.RankingScoreService
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
    lateinit var rankingScoreService: RankingScoreService

    @MockK
    lateinit var matchService: MatchService

    private val random = Random()

    private lateinit var rankingService: RankingService

    private val tournament = TournamentEntity()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")
    private val player4 = PlayerEntity(name = "Doro")

    @BeforeEach
    fun setUp() {
        rankingService = RankingService(rankingScoreService, random, matchService)
    }

    @Test
    fun rankPlayers() {
        val matchPlayer12 = PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 0, 2)
        val matchPlayer34 = PlayedMatch(UUID.randomUUID(), tournament, 1, player3, player4, 1, 2)


        val playerToMatches = mapOf(
            player1 to setOf(matchPlayer12),
            player2 to setOf(matchPlayer12),
            player3 to setOf(matchPlayer34),
            player4 to setOf(matchPlayer34)
        )
        val playerToScores = mapOf(
            player1 to RankingScore(0, 3.0, 0.0, 1.0),
            player2 to RankingScore(3, 0.0, 1.0, 1.0),
            player3 to RankingScore(0, 3.0, 1.0 / 3, 2.0 / 3),
            player4 to RankingScore(3, 0.0, 2.0 / 3, 1.0 / 3)
        )
        every { rankingScoreService.calculatePlayerScores(playerToMatches) } returns playerToScores

        val playersOrderedByRank = rankingService.rankPlayers(playerToMatches)

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
        val playerToMatches: Map<PlayerEntity, Set<PlayedMatch>> = mapOf(player1 to emptySet(), player2 to emptySet())
        val scorePlayer13 = RankingScore(0, 0.0, 0.0, 0.0)
        val scorePlayer24 = RankingScore(3, 0.0, 0.0, 0.0)
        val playerToScores = mapOf(
            player1 to scorePlayer13,
            player2 to scorePlayer24,
            player3 to scorePlayer13,
            player4 to scorePlayer24
        )
        every { rankingScoreService.calculatePlayerScores(playerToMatches) } returns playerToScores

        assertThat(scorePlayer24).isGreaterThan(scorePlayer13)

        val rankingService1 = RankingService(rankingScoreService, Random(1L), matchService)
        val rankingService2 = RankingService(rankingScoreService, Random(123743L), matchService)

        val playersOrderedByRank1 = rankingService1.rankPlayers(playerToMatches)
        assertThat(playersOrderedByRank1[0]).isEqualTo(player2)
        assertThat(playersOrderedByRank1[1]).isEqualTo(player4)
        assertThat(playersOrderedByRank1[2]).isEqualTo(player3)
        assertThat(playersOrderedByRank1[3]).isEqualTo(player1)

        val playersOrderedByRank2 = rankingService2.rankPlayers(playerToMatches)
        assertThat(playersOrderedByRank2[0]).isEqualTo(player4)
        assertThat(playersOrderedByRank2[1]).isEqualTo(player2)
        assertThat(playersOrderedByRank2[2]).isEqualTo(player1)
        assertThat(playersOrderedByRank2[3]).isEqualTo(player3)
    }

    @Test
    fun `rankPlayers with tournament input`() {
        val matchPlayer12 = MatchEntity(UUID.randomUUID(), tournament, 1, player1, player2, 0, 2)
        val matchPlayer34 = MatchEntity(UUID.randomUUID(), tournament, 1, player3, player4, 1, 2)
        val playedMatchPlayer12 = PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 0, 2)
        val playedMatchPlayer34 = PlayedMatch(UUID.randomUUID(), tournament, 1, player3, player4, 1, 2)
        val playedMatches = setOf(
            playedMatchPlayer12,
            playedMatchPlayer34
        )

        val playerToMatches = mapOf(
            player1 to setOf(playedMatchPlayer12),
            player2 to setOf(playedMatchPlayer12),
            player3 to setOf(playedMatchPlayer34),
            player4 to setOf(playedMatchPlayer34)
        )
        val playerToScores = mapOf(
            player1 to RankingScore(0, 3.0, 0.0, 1.0),
            player2 to RankingScore(3, 0.0, 1.0, 1.0),
            player3 to RankingScore(0, 3.0, 1.0 / 3, 2.0 / 3),
            player4 to RankingScore(3, 0.0, 2.0 / 3, 1.0 / 3)
        )

        val tournament = tournament.copy()
        tournament.matches.add(matchPlayer12)
        tournament.matches.add(matchPlayer34)

        every { rankingScoreService.calculatePlayerScores(playerToMatches) } returns playerToScores
        every { matchService.convertToPlayedMatches(tournament.matches) } returns playedMatches
        every { matchService.mapPlayersToMatches(tournament.getPlayers(), playedMatches) } returns playerToMatches

        val playersOrderedByRank = rankingService.rankPlayers(tournament)!!

        assertThat(playerToScores.getValue(player2)).isGreaterThan(playerToScores.getValue(player4))
        assertThat(playerToScores.getValue(player4)).isGreaterThan(playerToScores.getValue(player3))
        assertThat(playerToScores.getValue(player3)).isGreaterThan(playerToScores.getValue(player1))
        assertThat(playersOrderedByRank[0]).isEqualTo(player2)
        assertThat(playersOrderedByRank[1]).isEqualTo(player4)
        assertThat(playersOrderedByRank[2]).isEqualTo(player3)
        assertThat(playersOrderedByRank[3]).isEqualTo(player1)
    }
}