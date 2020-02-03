package com.tngtech.chsystem.entities

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class PlayerEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(unique = true)
    var name: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)