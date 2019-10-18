package com.tngtech.chsystem

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChSystemApplication

fun main(args: Array<String>) {
    runApplication<ChSystemApplication>(*args)
}