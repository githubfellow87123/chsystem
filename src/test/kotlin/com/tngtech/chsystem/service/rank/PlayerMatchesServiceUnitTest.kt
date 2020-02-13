package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class PlayerMatchesServiceUnitTest {

    private val playerMatchesService = PlayerMatchesService()

    private val tournament = TournamentEntity()

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")

    private val players = setOf(player1, player2, player3)

    @Test
    fun `mapPlayersToMatches no matches played`() {

        val playerToMatches = playerMatchesService.mapPlayersToMatches(players, emptySet())

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

        val playerToMatches = playerMatchesService.mapPlayersToMatches(players, playedMatches)

        assertThat(playerToMatches[player1]).containsExactlyInAnyOrder(match12, match13, match1Bye)
        assertThat(playerToMatches[player2]).containsExactlyInAnyOrder(match12, match23, match2Bye)
        assertThat(playerToMatches[player3]).containsExactlyInAnyOrder(match23, match13, match3Bye)
    }

    private fun createMatch(player1: PlayerEntity, player2: PlayerEntity?): PlayedMatch {
        return PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 0, 0)
    }
}