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

        return players.map { p -> PlayerModel(id = p.id, name = p.name, createdAt = p.createdAt) }
    }

    @PostMapping
    fun insertPlayer(@RequestBody playerModel: PlayerModel) {

        val player = PlayerEntity(name = playerModel.name)

        playerRepository.save(player)
    }
}