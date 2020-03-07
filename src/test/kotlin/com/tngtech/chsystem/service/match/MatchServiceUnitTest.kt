package com.tngtech.chsystem.service.match

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.time.LocalDateTime
import java.util.*

internal class MatchServiceUnitTest {

    private val matchService = MatchService()

    private val tournament = TournamentEntity()
    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bert")
    private val player3 = PlayerEntity(name = "Caesar")

    private val players = setOf(player1, player2, player3)

    @Test
    fun convertToPlayedMatches() {

        val match = MatchEntity(
            tournament = tournament,
            roundIndex = 1,
            player1 = player1,
            player2 = player2,
            winsPlayer1 = 2,
            winsPlayer2 = 0
        )

        val playedMatches = matchService.convertToPlayedMatches(setOf(match))
            ?: fail("Conversion should work in this case since results are entered")

        assertThat(playedMatches).hasSize(1)
        assertThat(playedMatches.first().id).isEqualTo(match.id)
        assertThat(playedMatches.first().tournament).isEqualTo(tournament)
        assertThat(playedMatches.first().roundIndex).isEqualTo(1)
        assertThat(playedMatches.first().player1).isEqualTo(player1)
        assertThat(playedMatches.first().player2).isEqualTo(player2)
        assertThat(playedMatches.first().winsPlayer1).isEqualTo(2)
        assertThat(playedMatches.first().winsPlayer2).isEqualTo(0)
    }

    @Test
    fun `convertToPlayedMatches a match with missing results`() {

        val match = MatchEntity(
            tournament = tournament,
            roundIndex = 1,
            player1 = player1,
            player2 = player2,
            winsPlayer1 = null,
            winsPlayer2 = null
        )

        val playedMatches = matchService.convertToPlayedMatches(setOf(match))

        assertThat(playedMatches).isNull()
    }

    @Test
    fun `mapPlayersToMatches no matches played`() {

        val playerToMatches = matchService.mapPlayersToMatches(players, emptySet())

        assertThat(playerToMatches[player1]).isEmpty()
        assertThat(playerToMatches[player2]).isEmpty()
        assertThat(playerToMatches[player3]).isEmpty()
    }

    @Test
    fun `mapPlayersToMatches some matches played`() {

        val match12 = createMatch(player1, player2)
        val match23 = createMatch(player2, player3)
        val match13 = createMatch(player1, player3)
        val match1Bye = createMatch(player1, null)
        val match2Bye = createMatch(player2, null)
        val match3Bye = createMatch(player3, null)

        val playedMatches: Set<PlayedMatch> = setOf(
            match12,
            match23,
            match13,
            match1Bye,
            match2Bye,
            match3Bye
        )

        val playerToMatches = matchService.mapPlayersToMatches(players, playedMatches)

        assertThat(playerToMatches[player1]).containsExactlyInAnyOrder(match12, match13, match1Bye)
        assertThat(playerToMatches[player2]).containsExactlyInAnyOrder(match12, match23, match2Bye)
        assertThat(playerToMatches[player3]).containsExactlyInAnyOrder(match23, match13, match3Bye)
    }

    private fun createMatch(player1: PlayerEntity, player2: PlayerEntity?): PlayedMatch {
        return PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 0, 0, LocalDateTime.now())
    }
}