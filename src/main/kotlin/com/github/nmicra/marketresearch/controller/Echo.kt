package com.github.nmicra.marketresearch.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class EchoController {

    @GetMapping("/echo/{txt}")
    suspend fun echo(@PathVariable txt : String) : String = "$txt !!!"

    @PostMapping("/echo2")
    suspend fun echo2(@RequestBody ech : Echo) : String = List(ech.time) { "${ech.txt} !!!" }.joinToString(",")
}

data class Echo(val txt : String, val time : Int)