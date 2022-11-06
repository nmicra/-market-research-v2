package com.github.nmicra.marketresearch.controller

import com.github.nmicra.marketresearch.service.MarketRawDataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "DataFetcher", description = "focus on fetching new data")
class DataFetcherController {

    @Autowired
    lateinit var marketRawDataService : MarketRawDataService


    /**
     * brings & saves the raw data
     * for a given label
     */
    @GetMapping("/raw/setup/{label}")
    @Operation(
        summary = "Setup RAW data for specified label",
        tags = ["DataFetcher"],
    )
    @ApiResponse(responseCode = "200", description = "Success response")
    suspend fun rawSetup(@PathVariable label : String) : String {
        marketRawDataService.setupNewMarketRawData(label)
        return "done ${System.currentTimeMillis()}"
    }

    @GetMapping("/raw/update/{label}")
    @Operation(
        summary = "Update RAW data for specified label",
        tags = ["DataFetcher"],
    )
    @ApiResponse(responseCode = "200", description = "Success response")
    suspend fun rawUpdate(@PathVariable label : String) : String {
        marketRawDataService.updateRawData(label)
        return "done ${System.currentTimeMillis()}"
    }
}