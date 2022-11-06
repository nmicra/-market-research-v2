package com.github.nmicra.marketresearch

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(info = Info(title = "MarketResearch", version = "1.0", description = "Enter /v3/api-docs into the search box"))
class MarketResearchApplication

fun main(args: Array<String>) {
	runApplication<MarketResearchApplication>(*args)
}
