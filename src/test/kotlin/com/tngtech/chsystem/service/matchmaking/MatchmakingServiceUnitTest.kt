package com.tngtech.chsystem.service.matchmaking

import com.tngtech.chsystem.dao.MatchRepository
import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.rank.RankingService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

@ExtendWith(MockKExtension::class)
internal class MatchmakingServiceUnitTest {

    @MockK
    lateinit var rankingService: RankingService

    @MockK
    lateinit var matchRepository: MatchRepository

    @MockK
    lateinit var pairingService: PairingService

    @MockK
    lateinit var matchService: MatchService

    @InjectMockKs
    lateinit var matchmakingService: MatchmakingService

    private val tournament = TournamentEntity()
    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bert")
    private val match = MatchEntity(UUID.randomUUID(), tournament, 1, player1, player2, 2, 0)
    private val playedMatch = PlayedMatch(UUID.randomUUID(), tournament, 1, player1, player2, 2, 0, LocalDateTime.now())

    @Test
    fun generateMatchesForNextRound() {

        val tournament = tournament.copy()
        tournament.matches.add(match)
        val playerToMatches = HashMap<PlayerEntity, Set<PlayedMatch>>()
        val rankedPlayers = listOf(player1, player2)
        val matchesForNextRoundSlot = slot<Set<MatchEntity>>()

        every { matchService.convertToPlayedMatches(tournament.matches) } returns setOf(playedMatch)
        every { matchService.mapPlayersToMatches(tournament.getPlayers(), any()) } returns playerToMatches
        every { rankingService.rankPlayers(playerToMatches) } returns rankedPlayers
        every { pairingService.generatePairingsForNextRound(rankedPlayers, playerToMatches) } returns listOf(0 to 1)
        every {
            matchRepository.saveAll(capture(matchesForNextRoundSlot))
        } answers {
            matchesForNextRoundSlot.captured
        }

        val code = matchmakingService.generateMatchesForNextRound(tournament)
        val matchesOfNextRound = matchesForNextRoundSlot.captured

        assertThat(code).isEqualTo(MatchmakingCode.SUCCESSFUL)
        assertThat(matchesOfNextRound).hasSize(1)
        assertThat(matchesOfNextRound.first().player1).isEqualTo(player1)
        assertThat(matchesOfNextRound.first().player2).isEqualTo(player2)
        assertThat(matchesOfNextRound.first().winsPlayer1).isNull()
        assertThat(matchesOfNextRound.first().winsPlayer2).isNull()
        assertThat(matchesOfNextRound.first().roundIndex).isEqualTo(tournament.roundIndex + 1)
    }

    @Test
    fun `generateMatchesForNextRound bye case`() {

        val tournament = tournament.copy()
        tournament.matches.add(match)
        val playerToMatches = HashMap<PlayerEntity, Set<PlayedMatch>>()
        val rankedPlayers = listOf(player1)
        val matchesForNextRoundSlot = slot<Set<MatchEntity>>()

        every { matchService.convertToPlayedMatches(tournament.matches) } returns setOf(playedMatch)
        every { matchService.mapPlayersToMatches(tournament.getPlayers(), any()) } returns playerToMatches
        every { rankingService.rankPlayers(playerToMatches) } returns rankedPlayers
        every { pairingService.generatePairingsForNextRound(rankedPlayers, playerToMatches) } returns listOf(0 to 1)
        every {
            matchRepository.saveAll(capture(matchesForNextRoundSlot))
        } answers {
            matchesForNextRoundSlot.captured
        }

        val code = matchmakingService.generateMatchesForNextRound(tournament)
        val matchesOfNextRound = matchesForNextRoundSlot.captured

        assertThat(code).isEqualTo(MatchmakingCode.SUCCESSFUL)
        assertThat(matchesOfNextRound).hasSize(1)
        assertThat(matchesOfNextRound.first().player1).isEqualTo(player1)
        assertThat(matchesOfNextRound.first().player2).isNull()
        assertThat(matchesOfNextRound.first().winsPlayer1).isNull()
        assertThat(matchesOfNextRound.first().winsPlayer2).isNull()
        assertThat(matchesOfNextRound.first().roundIndex).isEqualTo(tournament.roundIndex + 1)
    }

    @Test
    fun `generateMatchesForNextRound missing result of current round`() {

        val tournament = tournament.copy()
        val matchMissingResult = MatchEntity(UUID.randomUUID(), tournament, 1, player1, player2)
        tournament.matches.add(matchMissingResult)
        every { matchService.convertToPlayedMatches(tournament.matches) } returns null

        val code = matchmakingService.generateMatchesForNextRound(tournament)

        assertThat(code).isEqualTo(MatchmakingCode.MISSING_RESULTS_OF_CURRENT_ROUND)
    }

    @Test
    fun `generateMatchesForNextRound no valid matchmaking possible for next round`() {

        val tournament = tournament.copy()
        tournament.matches.add(match)
        val playerToMatches = HashMap<PlayerEntity, Set<PlayedMatch>>()
        val rankedPlayers = listOf(player1, player2)

        every { matchService.convertToPlayedMatches(tournament.matches) } returns setOf(playedMatch)
        every { matchService.mapPlayersToMatches(tournament.getPlayers(), any()) } returns playerToMatches
        every { rankingService.rankPlayers(playerToMatches) } returns rankedPlayers
        every { pairingService.generatePairingsForNextRound(rankedPlayers, playerToMatches) } returns null

        val code = matchmakingService.generateMatchesForNextRound(tournament)

        assertThat(code).isEqualTo(MatchmakingCode.NO_VALID_MATCHES_FOR_NEXT_ROUND_AVAILABLE)
    }
}