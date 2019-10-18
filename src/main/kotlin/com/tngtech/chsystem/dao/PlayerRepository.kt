package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.Player
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PlayerRepository : CrudRepository<Player, UUID> {
    fun findByName(name: String): Player?
}