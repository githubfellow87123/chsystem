package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.Score
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.score.ScoreService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class RankingServiceUnitTest {

    @MockK
    lateinit var scoreService: ScoreService

    @MockK
    lateinit var playerMatchesService: PlayerMatchesService

    @InjectMockKs
    lateinit var rankingService: RankingService

    private val tournament = TournamentEntity()

    @Test
    fun rankPlayers() {
        val player1 = PlayerEntity(name = "Alex")
        val player2 = PlayerEntity(name = "Bart")
        val player3 = PlayerEntity(name = "Caesar")
        val player4 = PlayerEntity(name = "Doro")

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

}