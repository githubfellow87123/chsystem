package com.tngtech.chsystem.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class ApplicationConfiguration {

    @Bean
    fun random(): Random {
        return Random()
    }

}