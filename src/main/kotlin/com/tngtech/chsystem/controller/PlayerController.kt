package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.model.PlayerModel
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("players")
class PlayerController(private val playerRepository: PlayerRepository) {

    @GetMapping
    fun findAll(): List<PlayerModel> {
        val players = playerRepository.findAll()

        return players.map { it.toPersonModel() }
    }

    @PostMapping
    fun insertPlayer(@RequestBody playerModel: PlayerModel): PlayerModel {

        val player = playerModel.toPlayerEntity()

        playerRepository.save(player)

        return player.toPersonModel()
    }

    fun PlayerEntity.toPersonModel() = PlayerModel(
        id = id,
        name = name,
        createdAt = createdAt
    )

    fun PlayerModel.toPlayerEntity() = PlayerEntity(
        name = name
    )
}