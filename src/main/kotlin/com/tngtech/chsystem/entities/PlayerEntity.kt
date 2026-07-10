package com.tngtech.chsystem.entities

import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "PLAYER")
data class PlayerEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    @Column(unique = true, nullable = false)
    val name: String,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)