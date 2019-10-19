package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.PlayerRepository
import com.tngtech.chsystem.entities.Player
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("players")
class PlayerController(private val playerRepository: PlayerRepository) {

    @GetMapping
    fun findAll() = playerRepository.findAll()

    @PostMapping
    fun insertPlayer(player: Player) {
        playerRepository.save(player);
    }
}