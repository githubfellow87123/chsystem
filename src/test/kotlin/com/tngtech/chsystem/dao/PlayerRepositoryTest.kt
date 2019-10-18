package com.tngtech.chsystem.dao

import com.tngtech.chsystem.entities.Player
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class PlayerRepositoryTest @Autowired constructor(
    val entityManager: TestEntityManager,
    val playerRepository: PlayerRepository
) {

    @Test
    fun `When findByLogin then return User`() {
        val bernd = Player("Bernd")
        entityManager.persist(bernd)
        entityManager.flush()
        val user = playerRepository.findByName("Bernd")
        assertThat(user).isEqualTo(bernd)
    }
}