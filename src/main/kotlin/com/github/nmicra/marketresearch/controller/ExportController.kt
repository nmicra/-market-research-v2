package com.github.nmicra.marketresearch.controller

//import org.apache.tomcat.util.http.fileupload.IOUtils
import com.github.nmicra.marketresearch.analysis.*
import com.github.nmicra.marketresearch.entity.LLHHPrediction
import com.github.nmicra.marketresearch.repository.LLHHPredictionRepository
import com.github.nmicra.marketresearch.repository.MarketRawDataRepository
import com.github.nmicra.marketresearch.service.TradingPeriodService
import com.github.nmicra.marketresearch.service.report.LLHHReportService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
class ExportController {

    @Autowired
    lateinit var marketRawDataRepository: MarketRawDataRepository

    @Autowired
    lateinit var LLHHReportService: LLHHReportService

    @Autowired
    lateinit var tradingPeriodService: TradingPeriodService

    @Autowired
    lateinit var llhhPredictionRepository: LLHHPredictionRepository

    val LLHHReportHeader =
        "date,close,mv5Dev,mv7Dev,currentIndexClassification,distanceToHH,distanceToLL,distanceToLH,distanceToHL,nextLLHH,nextLLHHDistance\n"

    val ReversalsReoportHeaders =
        "year,weekNr,open,high,low,close,volume,delta,intraVolatility,trend,scaledTrend,momentum," +
                "stochastic,bullishIndicators,bearishIndicators,bullishReversals,bullishElectedFlag," +
                "bearishReversals,bearishElectedFlag,mavg5,mavg7\n"

    /**
     * exports raw data to csv file
     * for a given label
     */
    @GetMapping("/export/raw/{label}", produces = ["application/octet-stream"])
    suspend fun exportRawToCsv(
        @PathVariable label: String,
        response: ServerHttpResponse
    ): Flow<ByteArray> {

        response.headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=$label-raw-${LocalDateTime.now()}.csv"
        )
        response.headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        response.headers.add("Pragma", "no-cache")
        response.headers.add("Expires", "0")


        return marketRawDataRepository.findAllByLabel(label).map {
            "${it.date},${it.close},${it.open},${it.low},${it.close}\n".toByteArray()
        }

    }


    @GetMapping("/export/reversals/{label}/{interval}", produces = ["application/octet-stream"])
    suspend fun exportReversalsToCsv(
        @PathVariable label: String,
        @PathVariable interval: String,
        response: ServerHttpResponse
    ): Flow<ByteArray> {

        val tradingData = runBlocking { tradingPeriodService.tradingPeriodByInterval(interval, label) }

        tradingData.calculateDelta()
        tradingData.scaleVolume()
        tradingData.calculateTrend()
        tradingData.scaleTrend()
        tradingData.calculateMomentum()
        tradingData.calculateStochastic()
        tradingData.calculateMA(5)
        tradingData.calculateMA(7)
        tradingData.identifyOutsideReversals()
        tradingData.identifyCandlePattern()
        tradingData.identifyEveningStarReversals()
        tradingData.identifyReversals()
        val dailyList = tradingData.filter { it.hasStochastic() } // Stochastic is for 14 days


        response.headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=$label-reversals-${LocalDateTime.now()}.csv"
        )
        response.headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        response.headers.add("Pragma", "no-cache")
        response.headers.add("Expires", "0")

        return flow {
            emit(ReversalsReoportHeaders.toByteArray()) // headers
            dailyList.map {
                ("${it.year},${it.weekNr},${it.open},${it.high},${it.low},${it.close},${it.volume},${it.delta},${it.intraVolatility}" +
                        ",${it.trend},${it.scaledTrend},${it.momentum},${it.stochastic}" +
                        ",\"[${
                            it.tradingLabelsList.filter { ind -> ind.isBullishIndicator() }.joinToString(",")
                        }]\"" +
                        ",\"[${
                            it.tradingLabelsList.filter { ind -> ind.isBearishIndicator() }.joinToString(",")
                        }]\"" +
                        ",${it.bullishReversals.reversed().joinToString(">")},${it.bullishElectedFlag}" +
                        ",${it.bearishReversals.joinToString(">")},${it.bearishElectedFlag}" +
                        ",${it.movingAvg[5]},${it.movingAvg[7]}\n").toByteArray()
            }.forEach { emit(it) }
        }

    }

    @GetMapping("/export/llhh/{label}/{interval}", produces = ["application/octet-stream"])
    suspend fun exportLLHHReportToCsv(
        @PathVariable label: String,
        @PathVariable interval: String,
        response: ServerHttpResponse
    ): Flow<ByteArray> {

        val tradingData = runBlocking { tradingPeriodService.tradingPeriodByInterval(interval, label) }

        val report = listOf(LLHHReportHeader) + LLHHReportService.createLLHHReportWithMV(tradingData)

        response.headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=$label-llhh-${LocalDateTime.now()}.csv"
        )
        response.headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
        response.headers.add("Pragma", "no-cache")
        response.headers.add("Expires", "0")
        return report.asFlow().map { it.toByteArray() }
    }


    @GetMapping("/exportdb/llhh/{label}/{interval}")
    suspend fun exportLLHHReportToDB(
        @PathVariable label: String,
        @PathVariable interval: String
    ): String {

        val tradingData = runBlocking { tradingPeriodService.tradingPeriodByInterval(interval, label) }

        LLHHReportService.createLLHHReportWithMV(tradingData)
            .dropLast(5) // Just for testing
            .asFlow()
            .map { it.removeSuffix("\r\n") }
            .map {
                val params = it.split(",")
                check(params.size == 11) { "was expecting 11 params, but got ${params.size}" }
                val dt = params[0].split("-")// year-month-day
                val llp = LLHHPrediction(
                    label = label,
                    interval = interval,
                    date = LocalDate.of(dt[0].toInt(), dt[1].toInt(), dt[2].toInt()),
                    close = params[1].toDouble(),
                    mv5Dev = params[2].toDouble(),
                    mv7Dev = params[3].toDouble(),
                    currentIndexClassification = params[4],
                    distanceToHH = params[5].toInt(),
                    distanceToLL = params[6].toInt(),
                    distanceToLH = params[7].toInt(),
                    distanceToHL = params[8].toInt(),
                    nextLLHH = params[9],
                    nextLLHHDistance = params[10].toInt()
                )
                println("1 >>> $llp")
                llp
            }.collect {
                println("2 >>> $it")
                llhhPredictionRepository.save(it)
            }

        return "Done-$label-$interval-${LocalDateTime.now()}"
    }
}