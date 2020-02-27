package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.MatchRepository
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.MatchResultModel
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class TournamentMatchControllerUnitTest {

    @RelaxedMockK
    private lateinit var matchRepository: MatchRepository

    @InjectMockKs
    private lateinit var tournamentMatchController: TournamentMatchController

    val player1 = PlayerEntity(name = "Alex")
    val player2 = PlayerEntity(name = "Bert")

    private val tournament = TournamentEntity(state = TournamentState.IN_PROGRESS, roundIndex = 2)
    private val match = MatchEntity(tournament = tournament, player1 = player1, player2 = player2, roundIndex = 2)
    private val matchResultModel = MatchResultModel(match.id, 1, 2)

    @Test
    fun enterMatchResult() {

        val matchSlot = slot<MatchEntity>()
        every { matchRepository.save(capture(matchSlot)) } answers { matchSlot.captured }
        every { matchRepository.findByIdOrNull(match.id) } returns match

        tournamentMatchController.enterMatchResult(tournament.id, match.id, matchResultModel)

        val savedMatch = matchSlot.captured
        verify { matchRepository.save(savedMatch) }
        assertThat(savedMatch.id).isEqualTo(match.id)
        assertThat(savedMatch.roundIndex).isEqualTo(2)
        assertThat(savedMatch.tournament).isEqualTo(tournament)
        assertThat(savedMatch.player1).isEqualTo(player1)
        assertThat(savedMatch.player2).isEqualTo(player2)
        assertThat(savedMatch.winsPlayer1).isEqualTo(1)
        assertThat(savedMatch.winsPlayer2).isEqualTo(2)
    }

    @Test
    fun `enterMatchResult throws exception when match id in body and path dont match`() {

        every { matchRepository.findByIdOrNull(match.id) } returns match

        assertFailsWith<TournamentMatchController.MatchMismatchException> {
            tournamentMatchController.enterMatchResult(tournament.id, UUID.randomUUID(), matchResultModel)
        }
    }

    @Test
    fun `enterMatchResult throws exception when match doesn't exist`() {

        every { matchRepository.findByIdOrNull(match.id) } returns null

        assertFailsWith<TournamentMatchController.MatchMismatchException> {
            tournamentMatchController.enterMatchResult(tournament.id, match.id, matchResultModel)
        }
    }

    @Test
    fun `enterMatchResult throws exception when tournament id does not belong to match`() {
        every { matchRepository.findByIdOrNull(match.id) } returns match

        assertFailsWith<TournamentMatchController.TournamentMismatchException> {
            tournamentMatchController.enterMatchResult(UUID.randomUUID(), match.id, matchResultModel)
        }
    }

    @Test
    fun `enterMatchResult throws exception when tournament is not in progress`() {
        val matchTournamentAlreadyDone = match.copy(tournament = tournament.copy(state = TournamentState.DONE))
        every { matchRepository.findByIdOrNull(match.id) } returns matchTournamentAlreadyDone

        assertFailsWith<TournamentMatchController.TournamentInWrongStateException> {
            tournamentMatchController.enterMatchResult(tournament.id, match.id, matchResultModel)
        }
    }

    @Test
    fun `enterMatchResult throws exception when round index of match and tournament are different`() {
        val matchFromLastRound = match.copy(roundIndex = 1)
        every { matchRepository.findByIdOrNull(match.id) } returns matchFromLastRound

        assertFailsWith<TournamentMatchController.TournamentInWrongStateException> {
            tournamentMatchController.enterMatchResult(tournament.id, match.id, matchResultModel)
        }
    }
}