package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface PlayerRepository : CrudRepository<PlayerEntity, UUID> {
    fun findByName(name: String): PlayerEntity?
}