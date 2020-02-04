package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.PlayerEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class PlayerRepositoryComponentTest(
    @Autowired val playerRepository: PlayerRepository
) {

    @Test
    fun `findByName finds player by name`() {
        val player = PlayerEntity(name = "Bernd")
        playerRepository.save(player)
        val foundPlayer = playerRepository.findByName("Bernd")
        assertThat(foundPlayer).isEqualTo(player)
    }
}