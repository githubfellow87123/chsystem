package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.entities.Player
import com.tngtech.chsystem.model.PlayerModel
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("players")
class PlayerController(private val playerRepository: PlayerRepository) {

    @GetMapping
    fun findAll(): List<PlayerModel> {
        val players = playerRepository.findAll()

        return players.map { p -> PlayerModel(p.name) }
    }

    @PostMapping
    fun insertPlayer(@RequestBody playerModel: PlayerModel) {

        val player = Player(name = playerModel.name)

        playerRepository.save(player)
    }
}