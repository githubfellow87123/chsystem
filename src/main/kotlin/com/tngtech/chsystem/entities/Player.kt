package com.tngtech.chsystem.entities

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Player(
    var name: String,
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @Id
    var id: UUID = UUID.randomUUID()
)

