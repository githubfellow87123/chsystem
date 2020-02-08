package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.TournamentModel
import com.tngtech.chsystem.service.MatchmakingService
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
        val match = MatchEntity(
            roundIndex = 1,
            player1 = player1,
            player2 = player2,
            tournament = tournamentEntity
        )
        val tournamentEntitySlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every {
            matchmakingService.generateMatchesForRound(
                1,
                setOf(player1, player2),
                emptySet()
            )
        } returns setOf(match)
        every { tournamentRepository.save(capture(tournamentEntitySlot)) } answers { tournamentEntitySlot.captured }

        val startedTournament = tournamentController.startTournament(tournamentEntity.id)

        assertThat(startedTournament.state).isEqualTo(TournamentState.IN_PROGRESS)
        assertThat(startedTournament.roundIndex).isEqualTo(1)
        assertThat(tournamentEntitySlot.captured.state).isEqualTo(TournamentState.IN_PROGRESS)
        assertThat(tournamentEntitySlot.captured.roundIndex).isEqualTo(1)
        assertThat(tournamentEntitySlot.captured.matches).containsExactly(match)
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
}