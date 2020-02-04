package com.tngtech.chsystem.controller

import com.tngtech.chsystem.model.PlayerModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlayerControllerIntegrationTest(
    @Autowired val restTemplate: TestRestTemplate
) {

    @Test
    fun `A new player can be created and fetched`() {
        val playerResponse = restTemplate.postForEntity(
            "/players", buildCreatePlayerRequest("Alex"), PlayerModel::class.java
        )
        val playersResponse = restTemplate.getForEntity("/players", List::class.java)

        assertThat(playerResponse.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(playerResponse.body?.name).isEqualTo("Alex")
        assertThat(playerResponse.body?.id).isNotNull()
        assertThat(playerResponse.body?.createdAt).isNotNull()

        assertThat(playersResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(playersResponse.body).hasSize(1);
    }

    fun buildCreatePlayerRequest(playerName: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val body = "{\"name\": \"$playerName\"}"
        return HttpEntity(body, headers)
    }
}