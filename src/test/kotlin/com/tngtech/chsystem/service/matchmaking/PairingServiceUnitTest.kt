package com.tngtech.chsystem.service.matchmaking

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.util.*

internal class PairingServiceUnitTest {

    private val pairingService = PairingService()

    private val tournament = TournamentEntity()

    companion object {

        private val player1 = PlayerEntity(name = "Alex")
        private val player2 = PlayerEntity(name = "Bert")
        private val player3 = PlayerEntity(name = "Caesar")
        private val player4 = PlayerEntity(name = "Doro")
        private val player5 = PlayerEntity(name = "Emil")
        private val player6 = PlayerEntity(name = "Flo")
        private val player7 = PlayerEntity(name = "Gerd")
        private val player8 = PlayerEntity(name = "Heinrich")

        private val ranked5Players = listOf(player1, player2, player3, player4, player5)
        private val ranked6Players = listOf(player1, player2, player3, player4, player5, player6)
        private val ranked8Players = listOf(player1, player2, player3, player4, player5, player6, player7, player8)

        @JvmStatic
        fun playedVsAnotherOneMatch() = listOf(
            Arguments.of(player1, player2, true),
            Arguments.of(player2, player1, true),
            Arguments.of(player1, null, false),
            Arguments.of(player1, player3, false)
        )
    }

    @Test
    fun `generatePairingsForNextRound no matches played, even number of players`() {

        val playerToMatches = mapOf(
            player1 to emptySet<PlayedMatch>(),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet(),
            player6 to emptySet()
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked6Players,
            playerToMatches
        )

        assertThat(pairings).containsExactly(0 to 1, 2 to 3, 4 to 5)
    }

    @Test
    fun `generatePairingsForNextRound no matches played, odd number of players`() {

        val playerToMatches = mapOf(
            player1 to emptySet<PlayedMatch>(),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked5Players,
            playerToMatches
        )

        assertThat(pairings).containsExactly(0 to 1, 2 to 3, 4 to 5)
    }

    @Test
    fun `generatePairingsForNextRound one matches played, even number of players`() {

        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, player6)),
            player2 to setOf(createMatch(player2, player5)),
            player3 to setOf(createMatch(player3, player4)),
            player4 to setOf(createMatch(player3, player4)),
            player5 to setOf(createMatch(player2, player5)),
            player6 to setOf(createMatch(player1, player6))
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked6Players,
            playerToMatches
        )

        assertThat(pairings).containsExactly(0 to 1, 2 to 4, 3 to 5)
    }

    @Test
    fun `generatePairingsForNextRound two matches played, odd number of players`() {

        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, null), createMatch(player3, player1)),
            player2 to setOf(createMatch(player3, player2), createMatch(player4, player2)),
            player3 to setOf(createMatch(player3, player2), createMatch(player3, player1)),
            player4 to setOf(createMatch(player5, player4), createMatch(player4, player2)),
            player5 to setOf(createMatch(player5, player4), createMatch(player5, null))
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked5Players,
            playerToMatches
        )

        assertThat(pairings).containsExactly(0 to 1, 2 to 4, 3 to 5)
    }

    @Test
    fun `generatePairingsForNextRound two matches need to be recreated, even number of players`() {

        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, player2), createMatch(player1, player5)),
            player2 to setOf(createMatch(player1, player2), createMatch(player2, player3)),
            player3 to setOf(createMatch(player3, player4), createMatch(player2, player3)),
            player4 to setOf(createMatch(player3, player4), createMatch(player4, player6)),
            player5 to setOf(createMatch(player5, player6), createMatch(player1, player5)),
            player6 to setOf(createMatch(player5, player6), createMatch(player4, player6))
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked6Players,
            playerToMatches
        )

        assertThat(pairings).containsExactly(0 to 2, 1 to 5, 3 to 4)
    }

    @Test
    fun `generatePairingsForNextRound two matches in a row need to be recreated`() {

        val playerToMatches = mapOf(
            player1 to setOf(
                createMatch(player1, player6),
                createMatch(player1, player3),
                createMatch(player1, player4)
            ),
            player2 to setOf(
                createMatch(player2, player4),
                createMatch(player2, player6),
                createMatch(player2, player5)
            ),
            player3 to setOf(
                createMatch(player3, player8),
                createMatch(player1, player3),
                createMatch(player3, player6)
            ),
            player4 to setOf(
                createMatch(player2, player4),
                createMatch(player4, player7),
                createMatch(player1, player4)
            ),
            player5 to setOf(
                createMatch(player5, player7),
                createMatch(player5, player8),
                createMatch(player2, player5)
            ),
            player6 to setOf(
                createMatch(player1, player6),
                createMatch(player2, player6),
                createMatch(player3, player6)
            ),
            player7 to setOf(
                createMatch(player5, player7),
                createMatch(player4, player7),
                createMatch(player7, player8)
            ),
            player8 to setOf(
                createMatch(player3, player8),
                createMatch(player5, player8),
                createMatch(player7, player8)
            )
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked8Players,
            playerToMatches
        )

        assertThat(pairings).containsExactly(0 to 1, 2 to 4, 3 to 7, 5 to 6)
    }

    @Test
    fun `generatePairingsForNextRound unable to create pairing`() {

        val playerToMatches = mapOf(
            player1 to setOf(
                createMatch(player1, player6),
                createMatch(player1, player3),
                createMatch(player1, player2)
            ),
            player2 to setOf(
                createMatch(player2, player5),
                createMatch(player2, player4),
                createMatch(player1, player2)
            ),
            player3 to setOf(
                createMatch(player3, player4),
                createMatch(player1, player3),
                createMatch(player3, player5)
            ),
            player4 to setOf(
                createMatch(player3, player4),
                createMatch(player2, player4),
                createMatch(player4, player6)
            ),
            player5 to setOf(
                createMatch(player2, player5),
                createMatch(player5, player6),
                createMatch(player3, player5)
            ),
            player6 to setOf(
                createMatch(player1, player6),
                createMatch(player5, player6),
                createMatch(player4, player6)
            )
        )

        val pairings = pairingService.generatePairingsForNextRound(
            ranked6Players,
            playerToMatches
        )

        assertThat(pairings).isNull()
    }

    @Test
    fun `generatePairingsForNextRound unable to create pairing cause all players played vs another`() {

        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, player2)),
            player2 to setOf(createMatch(player1, player2))
        )

        val pairings = pairingService.generatePairingsForNextRound(
            listOf(player1, player2),
            playerToMatches
        )

        assertThat(pairings).isNull()
    }

    @ParameterizedTest
    @CsvSource("0,1,0,1", "1,4,1,4", "3,5,3,5")
    fun `assignOpponent no matches played, no players already assigned`(
        rankOfPlayerSearchingForOpponent: Int,
        startingRankOfPossibleOpponent: Int,
        expectedFirstRankOfPlayerInPair: Int,
        expectedSecondRankOfPlayerInPair: Int
    ) {
        val playerToMatches = mapOf(
            player1 to emptySet<PlayedMatch>(),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet(),
            player6 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            rankOfPlayerSearchingForOpponent,
            startingRankOfPossibleOpponent,
            emptySet(),
            ranked6Players,
            playerToMatches
        )

        assertThat(assignedPair?.first).isEqualTo(expectedFirstRankOfPlayerInPair)
        assertThat(assignedPair?.second).isEqualTo(expectedSecondRankOfPlayerInPair)
    }

    @Test
    fun `assignOpponent even number of players no bye`() {
        val playerToMatches = mapOf(
            player1 to emptySet<PlayedMatch>(),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet(),
            player6 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            5,
            6,
            emptySet(),
            ranked6Players,
            playerToMatches
        )

        assertThat(assignedPair).isNull()
    }

    @Test
    fun `assignOpponent odd number of players bye case`() {
        val playerToMatches = mapOf(
            player1 to emptySet<PlayedMatch>(),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            4,
            5,
            emptySet(),
            ranked5Players,
            playerToMatches
        )

        assertThat(assignedPair?.first).isEqualTo(4)
        assertThat(assignedPair?.second).isEqualTo(5)
    }

    @Test
    fun `assignOpponent if next opponent already played vs player he will be skipped`() {
        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, player2)),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            0,
            1,
            emptySet(),
            ranked5Players,
            playerToMatches
        )

        assertThat(assignedPair?.first).isEqualTo(0)
        assertThat(assignedPair?.second).isEqualTo(2)
    }

    @Test
    fun `assignOpponent if next opponents already played vs player they will be skipped`() {
        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, player3), createMatch(player2, player1)),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            0,
            1,
            emptySet(),
            ranked5Players,
            playerToMatches
        )

        assertThat(assignedPair?.first).isEqualTo(0)
        assertThat(assignedPair?.second).isEqualTo(3)
    }

    @Test
    fun `assignOpponent if player already had a bye he will not get an other`() {
        val playerToMatches = mapOf(
            player1 to setOf(createMatch(player1, null)),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            0,
            5,
            emptySet(),
            ranked5Players,
            playerToMatches
        )

        assertThat(assignedPair).isNull()
    }

    @Test
    fun `assignOpponent if next opponent is already assigned to an other match he will be skipped`() {
        val playerToMatches = mapOf(
            player1 to emptySet<PlayedMatch>(),
            player2 to emptySet(),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            1,
            2,
            setOf(0, 2),
            ranked5Players,
            playerToMatches
        )

        assertThat(assignedPair?.first).isEqualTo(1)
        assertThat(assignedPair?.second).isEqualTo(3)
    }

    @Test
    fun `assignOpponent skip both "played vs another" and "already has a match assigend"`() {
        val playerToMatches = mapOf(
            player1 to emptySet(),
            player2 to setOf(createMatch(player4, player2)),
            player3 to emptySet(),
            player4 to emptySet(),
            player5 to emptySet()
        )

        val assignedPair = pairingService.assignOpponent(
            1,
            4,
            setOf(0, 2),
            ranked5Players,
            playerToMatches
        )

        assertThat(assignedPair?.first).isEqualTo(1)
        assertThat(assignedPair?.second).isEqualTo(4)
    }

    @Test
    fun `playedVsAnother no matches played`() {
        val playerToMatches = mapOf(player1 to emptySet<PlayedMatch>(), player2 to emptySet())
        assertThat(pairingService.playedVsAnother(player1, player2, playerToMatches)).isFalse()
    }


    @ParameterizedTest
    @MethodSource("playedVsAnotherOneMatch")
    fun `playedVsAnother played one match`(
        firstPlayerOfMatch: PlayerEntity,
        secondPlayerOfMatch: PlayerEntity?,
        player1PlayedVsPlayer2: Boolean
    ) {
        val playerToMatches = mapOf(
            player1 to setOf(createMatch(firstPlayerOfMatch, secondPlayerOfMatch))
        )

        assertThat(pairingService.playedVsAnother(player1, player2, playerToMatches)).isEqualTo(
            player1PlayedVsPlayer2
        )
    }

    @Test
    fun `playedVsAnother multiple matches`() {
        val playerToMatches = mapOf(
            player1 to setOf(
                createMatch(player1, player3),
                createMatch(player1, null),
                createMatch(player2, player1)
            )
        )

        assertThat(pairingService.playedVsAnother(player1, player2, playerToMatches)).isTrue()
    }

    private fun createMatch(player1: PlayerEntity, player2: PlayerEntity?): PlayedMatch {
        return PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 0, 0, LocalDateTime.now())
    }
}