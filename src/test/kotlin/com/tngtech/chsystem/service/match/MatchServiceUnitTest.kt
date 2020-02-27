package com.tngtech.chsystem.service.match

import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class MatchServiceUnitTest {

    private val matchService = MatchService()

    private val tournament = TournamentEntity()
    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bert")

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
}