package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.model.PlayerModel
import mu.KotlinLogging
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

fun PlayerEntity.toPlayerModel() = PlayerModel(
    id = id,
    name = name,
    createdAt = createdAt
)

fun PlayerModel.toPlayerEntity() = PlayerEntity(
    name = name
)

@RestController
@RequestMapping("players")
class PlayerController(
    private val playerRepository: PlayerRepository
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun findAllPlayers(): List<PlayerModel> {

        val players = playerRepository.findAll()

        return players.map { it.toPlayerModel() }
    }

    @GetMapping("{id}")
    fun findPlayerById(@PathVariable id: UUID): PlayerModel {

        val player = playerRepository.findById(id)

        if (player.isEmpty) {
            throw PlayerNotFoundException(id)
        }

        return player.get().toPlayerModel()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun insertPlayer(@RequestBody playerModel: PlayerModel): PlayerModel {

        val player = playerModel.toPlayerEntity()

        val playerInDb = playerRepository.findByName(playerModel.name)
        if (playerInDb != null) {
            throw DuplicatePlayerNameException(playerModel.name)
        }

        val playerEntity = playerRepository.save(player)
        logger.info { "Created new player $playerEntity" }

        return player.toPlayerModel()
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateOrInsertPlayer(
        @PathVariable id: UUID,
        @RequestBody playerModel: PlayerModel
    ): PlayerModel {

        if (playerModel.id != null && playerModel.id != id) {
            throw PlayerIdMismatchException(id, playerModel.id)
        }

        val playerEntityOptional = playerRepository.findById(id)
        val playerEntity: PlayerEntity
        val updatedPlayerEntity: PlayerEntity

        if (playerEntityOptional.isPresent) {
            playerEntity = playerEntityOptional.get()
            updatedPlayerEntity = playerEntity.copy(name = playerModel.name)
        } else {
            updatedPlayerEntity = PlayerEntity(id = id, name = playerModel.name)
        }

        playerRepository.save(updatedPlayerEntity)

        return updatedPlayerEntity.toPlayerModel()
    }

    @DeleteMapping("{id}")
    fun deletePlayer(@PathVariable id: UUID) {

        try {
            playerRepository.deleteById(id)
        } catch (e: EmptyResultDataAccessException) {
            throw PlayerNotFoundException(id)
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class PlayerNotFoundException(id: UUID) : RuntimeException("No player found with id: $id")

    @ResponseStatus(HttpStatus.CONFLICT)
    class DuplicatePlayerNameException(name: String) : RuntimeException("A player with name '$name' already exists")

    @ResponseStatus(HttpStatus.CONFLICT)
    class PlayerIdMismatchException(pathVariableId: UUID, requestBodyId: UUID) :
        RuntimeException("Id mismatch between path  $pathVariableId and request body $requestBodyId")
}
