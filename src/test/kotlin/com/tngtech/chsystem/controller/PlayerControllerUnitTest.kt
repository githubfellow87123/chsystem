package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.model.PlayerModel
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.dao.EmptyResultDataAccessException
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class PlayerControllerUnitTest {

    @MockK
    lateinit var playerRepository: PlayerRepository

    @InjectMockKs
    lateinit var playerController: PlayerController

    @Test
    fun findAllPlayers() {
        every { playerRepository.findAll() } returns arrayListOf(PlayerEntity(name = "Alex"))

        val players = playerController.findAllPlayers()

        verify { playerRepository.findAll() }
        assertThat(players).hasSize(1)
        assertThat(players[0].name).isEqualTo("Alex")
    }

    @Test
    fun findPlayerById() {

        val playerEntity = PlayerEntity(name = "Alex")
        every { playerRepository.findById(playerEntity.id) } returns Optional.of(playerEntity)

        val playerModel = playerController.findPlayerById(playerEntity.id)

        verify { playerRepository.findById(playerEntity.id) }
        assertThat(playerModel.name).isEqualTo("Alex")
    }

    @Test
    fun `findPlayerById throws exception if player with id does not exist`() {
        val id = UUID.randomUUID()
        every { playerRepository.findById(id) } returns Optional.empty()

        assertFailsWith(PlayerController.PlayerNotFoundException::class) {
            playerController.findPlayerById(id)
        }
    }

    @Test
    fun insertPlayer() {
        val playerModel = PlayerModel(null, "Alex", null)
        val playerEntitySlot = slot<PlayerEntity>()

        every { playerRepository.findByName("Alex") }.returns(null)
        every {
            playerRepository.save(capture(playerEntitySlot))
        } answers {
            playerEntitySlot.captured
        }

        playerController.insertPlayer(playerModel)

        verify { playerRepository.findByName("Alex") }
        verify { playerRepository.save(playerEntitySlot.captured) }
        assertThat(playerEntitySlot.captured.name).isEqualTo("Alex")
    }

    @Test
    fun `insertPlayer throws exception if a player with the same name already exist`() {
        val playerModel = PlayerModel(null, "Alex", null)
        every { playerRepository.findByName("Alex") }.returns(PlayerEntity(name = "Alex"))

        assertFailsWith(PlayerController.DuplicatePlayerNameException::class) {
            playerController.insertPlayer(playerModel)
        }
    }

    companion object {
        @JvmStatic
        fun modelIds(): Stream<Arguments> =
            Stream.of(
                Arguments.of(null),
                Arguments.of(UUID.randomUUID())
            )
    }

    @ParameterizedTest
    @MethodSource("modelIds")
    fun `updateOrInsertPlayer inserts when player with id does not exist`(playerModelId: UUID?) {
        val pathVariableId = playerModelId ?: UUID.randomUUID()
        val playerModel = PlayerModel(playerModelId, "Alex", null)
        every { playerRepository.findById(pathVariableId) }.returns(Optional.empty())
        val playerEntitySlot = slot<PlayerEntity>()

        every {
            playerRepository.save(capture(playerEntitySlot))
        } answers {
            playerEntitySlot.captured
        }

        playerController.updateOrInsertPlayer(pathVariableId, playerModel)

        val playerEntity = playerEntitySlot.captured
        verify { playerRepository.save(playerEntity) }
        assertThat(playerEntity.id).isEqualTo(pathVariableId)
        assertThat(playerEntity.name).isEqualTo("Alex")
    }

    @Test
    fun `updateOrInsertPlayer updates player name when player with id already exist`() {
        val id = UUID.randomUUID()
        val playerModel = PlayerModel(id, "Alex", null)
        every { playerRepository.findById(id) }.returns(Optional.of(PlayerEntity(id, "Berta")))
        val playerEntitySlot = slot<PlayerEntity>()

        every {
            playerRepository.save(capture(playerEntitySlot))
        } answers {
            playerEntitySlot.captured
        }

        playerController.updateOrInsertPlayer(id, playerModel)

        val playerEntity = playerEntitySlot.captured
        verify { playerRepository.save(playerEntity) }
        assertThat(playerEntity.id).isEqualTo(id)
        assertThat(playerEntity.name).isEqualTo("Alex")
    }

    @Test
    fun `updateOrInsertPlayer throws exception if id of model and path variable mismatch`() {
        val idPathVariable = UUID.randomUUID()
        val idModel = UUID.randomUUID()
        val playerModel = PlayerModel(idModel, "Alex", null)

        assertFailsWith<PlayerController.PlayerIdMismatchException> {
            playerController.updateOrInsertPlayer(idPathVariable, playerModel)
        }
    }

    @Test
    fun deletePlayer() {
        val id = UUID.randomUUID()
        every {
            playerRepository.deleteById(any())
        } answers {
            println("Deleted player with id $id")
        }

        playerController.deletePlayer(id)

        verify { playerRepository.deleteById(id) }
    }

    @Test
    fun `deletePlayer throws exception if player doesn't exist`() {
        val id = UUID.randomUUID()
        every {
            playerRepository.deleteById(any())
        } throws EmptyResultDataAccessException(1)

        assertFailsWith<PlayerController.PlayerNotFoundException> {
            playerController.deletePlayer(id)
        }
    }
}