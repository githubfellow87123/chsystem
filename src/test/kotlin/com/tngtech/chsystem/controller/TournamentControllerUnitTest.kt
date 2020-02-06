package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.TournamentModel
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class TournamentControllerUnitTest {

    @MockK
    lateinit var tournamentRepository: TournamentRepository

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
        Assertions.assertThat(tournamentEntity.id).isNotNull()
        Assertions.assertThat(tournamentEntity.date).isToday()
        Assertions.assertThat(tournamentEntity.roundIndex).isEqualTo(0)
        Assertions.assertThat(tournamentEntity.state).isEqualTo(TournamentState.INITIALIZING)
    }
}