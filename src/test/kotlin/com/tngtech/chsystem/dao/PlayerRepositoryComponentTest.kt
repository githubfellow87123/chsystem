package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.PlayerEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class PlayerRepositoryComponentTest @Autowired constructor(
    val entityManager: TestEntityManager,
    val playerRepository: PlayerRepository
) {

    @Test
    fun `When findByLogin then return User`() {
        val player = PlayerEntity(name = "Bernd")
        entityManager.persist(player)
        entityManager.flush()
        val user = playerRepository.findByName("Bernd")
        assertThat(user).isEqualTo(player)
    }
}