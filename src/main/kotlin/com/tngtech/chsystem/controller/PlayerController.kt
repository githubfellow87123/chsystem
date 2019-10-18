package com.tngtech.chsystem.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PlayerController {

    @GetMapping("/")
    fun blog(model: Model): String {
        model["name"] = "Bernd"
        return "user"
    }
}