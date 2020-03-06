package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class StatisticScoreServiceTest {

    private val statisticScoreService = StatisticScoreService(MatchService())

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bart")
    private val player3 = PlayerEntity(name = "Caesar")
    private val player4 = PlayerEntity(name = "Doro")

    lateinit var tournament: TournamentEntity

    @BeforeEach
    fun init() {
        tournament = TournamentEntity()
        tournament.addPlayer(player1)
        tournament.addPlayer(player2)
        tournament.addPlayer(player3)
        tournament.addPlayer(player4)
    }


    @Test
    fun calculateScores() {

        val match12 = MatchEntity(UUID.randomUUID(), tournament, 1, player1, player2, 0, 2)
        val match34 = MatchEntity(UUID.randomUUID(), tournament, 1, player3, player4, 2, 1)
        val match13 = MatchEntity(UUID.randomUUID(), tournament, 1, player1, player3, 1, 1)
        val match24 = MatchEntity(UUID.randomUUID(), tournament, 1, player2, player4, 2, 1)
        tournament.matches.add(match12)
        tournament.matches.add(match34)
        tournament.matches.add(match13)
        tournament.matches.add(match24)

        val playerToStatisticScores = statisticScoreService.calculateScores(tournament)

        assertThat(playerToStatisticScores.getValue(player1).matchWins).isEqualTo(0)
        assertThat(playerToStatisticScores.getValue(player1).matchLosses).isEqualTo(1)
        assertThat(playerToStatisticScores.getValue(player1).matchDraws).isEqualTo(1)
        assertThat(playerToStatisticScores.getValue(player1).gameWins).isEqualTo(1)
        assertThat(playerToStatisticScores.getValue(player1).gameLosses).isEqualTo(3)

        assertThat(playerToStatisticScores.getValue(player2).matchWins).isEqualTo(2)
        assertThat(playerToStatisticScores.getValue(player2).matchLosses).isEqualTo(0)
        assertThat(playerToStatisticScores.getValue(player2).matchDraws).isEqualTo(0)
        assertThat(playerToStatisticScores.getValue(player2).gameWins).isEqualTo(4)
        assertThat(playerToStatisticScores.getValue(player2).gameLosses).isEqualTo(1)

        assertThat(playerToStatisticScores.getValue(player3).matchWins).isEqualTo(1)
        assertThat(playerToStatisticScores.getValue(player3).matchLosses).isEqualTo(0)
        assertThat(playerToStatisticScores.getValue(player3).matchDraws).isEqualTo(1)
        assertThat(playerToStatisticScores.getValue(player3).gameWins).isEqualTo(3)
        assertThat(playerToStatisticScores.getValue(player3).gameLosses).isEqualTo(2)

        assertThat(playerToStatisticScores.getValue(player4).matchWins).isEqualTo(0)
        assertThat(playerToStatisticScores.getValue(player4).matchLosses).isEqualTo(2)
        assertThat(playerToStatisticScores.getValue(player4).matchDraws).isEqualTo(0)
        assertThat(playerToStatisticScores.getValue(player4).gameWins).isEqualTo(2)
        assertThat(playerToStatisticScores.getValue(player4).gameLosses).isEqualTo(4)
    }
}