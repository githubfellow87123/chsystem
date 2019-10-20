package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.model.PlayerModel
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("players")
class PlayerController(private val playerRepository: PlayerRepository) {

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

        playerRepository.save(player)

        return player.toPlayerModel()
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class PlayerNotFoundException(id: UUID) : RuntimeException("No player found with id: $id")

    fun PlayerEntity.toPlayerModel() = PlayerModel(
        id = id,
        name = name,
        createdAt = createdAt
    )

    fun PlayerModel.toPlayerEntity() = PlayerEntity(
        name = name
    )
}