package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.TournamentModel
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.matchmaking.MatchmakingCode
import com.tngtech.chsystem.service.matchmaking.MatchmakingService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class TournamentControllerUnitTest {

    @MockK
    lateinit var tournamentRepository: TournamentRepository

    @MockK
    lateinit var matchmakingService: MatchmakingService

    @MockK
    lateinit var matchService: MatchService

    @InjectMockKs
    lateinit var tournamentController: TournamentController

    @Test
    fun insertPlayer() {
        val tournamentEntitySlot = slot<TournamentEntity>()

        every {
            tournamentRepository.save(capture(tournamentEntitySlot))
        } answers {
            tournamentEntitySlot.captured
        }

        tournamentController.insertTournament(TournamentModel())

        val tournamentEntity = tournamentEntitySlot.captured
        verify { tournamentRepository.save(tournamentEntity) }
        assertThat(tournamentEntity.id).isNotNull()
        assertThat(tournamentEntity.date).isToday()
        assertThat(tournamentEntity.roundIndex).isEqualTo(0)
        assertThat(tournamentEntity.state).isEqualTo(TournamentState.INITIALIZING)
    }

    @Test
    fun `date is settable with insertPlayer`() {
        val tournamentEntitySlot = slot<TournamentEntity>()
        val date = LocalDate.of(2000, 12, 27)

        every {
            tournamentRepository.save(capture(tournamentEntitySlot))
        } answers {
            tournamentEntitySlot.captured
        }

        tournamentController.insertTournament(TournamentModel(date = date))

        val tournamentEntity = tournamentEntitySlot.captured
        verify { tournamentRepository.save(tournamentEntity) }
        assertThat(tournamentEntity.id).isNotNull()
        assertThat(tournamentEntity.date).isEqualTo(date)
        assertThat(tournamentEntity.roundIndex).isEqualTo(0)
        assertThat(tournamentEntity.state).isEqualTo(TournamentState.INITIALIZING)
    }

    @Test
    fun startTournament() {
        val player1 = PlayerEntity(name = "Alex")
        val player2 = PlayerEntity(name = "Bert")
        val players = setOf(player1, player2)
        val tournamentEntity = TournamentEntity(players = players)

        val tournamentEntitySlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { tournamentRepository.save(capture(tournamentEntitySlot)) } answers { tournamentEntitySlot.captured }

        val startedTournament = tournamentController.startTournament(tournamentEntity.id)

        assertThat(startedTournament.state).isEqualTo(TournamentState.IN_PROGRESS)
        assertThat(startedTournament.roundIndex).isEqualTo(0)
        assertThat(tournamentEntitySlot.captured.state).isEqualTo(TournamentState.IN_PROGRESS)
        assertThat(tournamentEntitySlot.captured.roundIndex).isEqualTo(0)
    }

    @Test
    fun `startTournament throws exception if tournament doesn't exist`() {

        val tournamentEntity = TournamentEntity()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.startTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun `startTournament throws exception if tournament is already finished`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.DONE)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.startTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun nextRoundOfTournament() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS, roundIndex = 1)
        val tournamentEntitySlot = slot<TournamentEntity>()

        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchmakingService.generateMatchesForNextRound(tournamentEntity) } returns MatchmakingCode.SUCCESSFUL
        every { tournamentRepository.save(capture(tournamentEntitySlot)) } answers { tournamentEntitySlot.captured }

        val tournamentModel = tournamentController.nextRoundOfTournament(tournamentEntity.id)

        assertThat(tournamentModel.roundIndex).isEqualTo(2)
        assertThat(tournamentEntitySlot.captured.roundIndex).isEqualTo(2)
    }

    @Test
    fun `nextRoundOfTournament throws exception if tournament doesn't exist`() {

        val tournamentEntity = TournamentEntity()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.nextRoundOfTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if matches can not be generated`() {
        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)

        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchmakingService.generateMatchesForNextRound(tournamentEntity) } returns MatchmakingCode.NO_VALID_MATCHES_FOR_NEXT_ROUND_AVAILABLE

        assertFailsWith<TournamentController.UnableToGenerateMatchesException> {
            tournamentController.nextRoundOfTournament(tournamentEntity.id)
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if results of current round were not entered`() {
        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)

        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchmakingService.generateMatchesForNextRound(tournamentEntity) } returns MatchmakingCode.MISSING_RESULTS_OF_CURRENT_ROUND

        assertFailsWith<TournamentController.UnableToGenerateMatchesException> {
            tournamentController.nextRoundOfTournament(tournamentEntity.id)
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if tournament is already finished`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.DONE)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.nextRoundOfTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if tournament has not started`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.INITIALIZING)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.nextRoundOfTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun finishTournament() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)
        val savedTournamentSlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns false
        every { tournamentRepository.save(capture(savedTournamentSlot)) } answers {
            savedTournamentSlot.captured
        }


        val finishedTournament = tournamentController.finishTournament(tournamentEntity.id)

        verify { tournamentRepository.save(savedTournamentSlot.captured) }
        assertThat(savedTournamentSlot.captured.state).isEqualTo(TournamentState.DONE)
        assertThat(finishedTournament.state).isEqualTo(TournamentState.DONE)
    }

    @Test
    fun `finishTournament throws exception when not all matches have results`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns true

        assertFailsWith<TournamentController.UnableToFinishTournamentException> {
            tournamentController.finishTournament(
                tournamentEntity.id
            )
        }
        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
    }

    @Test
    fun `finishTournament throws exception when tournament doesn't exist`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.finishTournament(
                tournamentEntity.id
            )
        }
        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
    }

    @Test
    fun `finishTournament throws exception when tournament is in wrong state`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.INITIALIZING)
        val savedTournamentSlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns false
        every { tournamentRepository.save(capture(savedTournamentSlot)) } answers {
            savedTournamentSlot.captured
        }

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.finishTournament(
                tournamentEntity.id
            )
        }
        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
    }

    @Test
    fun `finishTournament tournament can be finished twice`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.DONE)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns false

        val finishedTournament = tournamentController.finishTournament(tournamentEntity.id)

        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
        assertThat(finishedTournament.state).isEqualTo(TournamentState.DONE)
    }
}