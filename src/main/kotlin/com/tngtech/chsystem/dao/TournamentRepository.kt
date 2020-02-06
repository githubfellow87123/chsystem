package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.TournamentEntity
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TournamentRepository : CrudRepository<TournamentEntity, UUID>