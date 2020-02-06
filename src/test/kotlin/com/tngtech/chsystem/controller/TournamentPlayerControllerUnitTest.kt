package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.model.AssignPlayerToTournamentModel
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.*
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class TournamentPlayerControllerUnitTest {

    @InjectMockKs
    lateinit var tournamentPlayerController: TournamentPlayerController

    @MockK
    lateinit var tournamentRepository: TournamentRepository

    @MockK
    lateinit var playerRepository: PlayerRepository

    @Test
    fun `assignPlayerToTournament`() {
        val tournament = TournamentEntity()
        val player = PlayerEntity(name = "Alex")
        val assignPlayerToTournamentModel = AssignPlayerToTournamentModel(tournament.id, player.id)
        val tournamentSlot = slot<TournamentEntity>()

        every { playerRepository.findByIdOrNull(player.id) } returns player
        every { tournamentRepository.findByIdOrNull(tournament.id) } returns tournament
        every {
            tournamentRepository.save(capture(tournamentSlot))
        } answers {
            tournamentSlot.captured
        }

        tournamentPlayerController.assignPlayerToTournament(
            tournament.id,
            player.id,
            assignPlayerToTournamentModel
        )

        assertThat(tournamentSlot.captured.players).containsExactly(player)
    }

    @Test
    fun `assignPlayerToTournament throws exception if player id mismatches`() {
        val tournament = TournamentEntity()
        val player = PlayerEntity(name = "Alex")
        val assignPlayerToTournamentModel = AssignPlayerToTournamentModel(tournament.id, UUID.randomUUID())

        assertFailsWith<TournamentPlayerController.PlayerMismatchException> {
            tournamentPlayerController.assignPlayerToTournament(
                tournament.id,
                player.id,
                assignPlayerToTournamentModel
            )
        }
    }

    @Test
    fun `assignPlayerToTournament throws exception if tournament id mismatches`() {
        val tournament = TournamentEntity()
        val player = PlayerEntity(name = "Alex")
        val assignPlayerToTournamentModel = AssignPlayerToTournamentModel(UUID.randomUUID(), player.id)

        assertFailsWith<TournamentPlayerController.TournamentMismatchException> {
            tournamentPlayerController.assignPlayerToTournament(
                tournament.id,
                player.id,
                assignPlayerToTournamentModel
            )
        }
    }

    @Test
    fun `assignPlayerToTournament throws exception if player doesn't exist`() {
        val tournament = TournamentEntity()
        val playerId = UUID.randomUUID()
        val assignPlayerToTournamentModel = AssignPlayerToTournamentModel(tournament.id, playerId)

        every { playerRepository.findByIdOrNull(playerId) } returns null
        every { tournamentRepository.findByIdOrNull(tournament.id) } returns tournament

        assertFailsWith<TournamentPlayerController.PlayerMismatchException> {
            tournamentPlayerController.assignPlayerToTournament(
                tournament.id,
                playerId,
                assignPlayerToTournamentModel
            )
        }
    }

    @Test
    fun `assignPlayerToTournament throws exception if tournament doesn't exist`() {
        val tournamentId = UUID.randomUUID()
        val player = PlayerEntity(name = "Alex")
        val assignPlayerToTournamentModel = AssignPlayerToTournamentModel(tournamentId, player.id)

        every { playerRepository.findByIdOrNull(player.id) } returns player
        every { tournamentRepository.findByIdOrNull(tournamentId) } returns null

        assertFailsWith<TournamentPlayerController.TournamentMismatchException> {
            tournamentPlayerController.assignPlayerToTournament(
                tournamentId,
                player.id,
                assignPlayerToTournamentModel
            )
        }
    }
}