package com.github.nmicra.marketresearch.controller

import com.github.nmicra.marketresearch.ml.predictor.RegressionPrecision
import com.github.nmicra.marketresearch.ml.predictor.XGBoostTimeSeriesPredictor
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHDistancePredictor
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHMVPredictor
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHPredictor
import com.github.nmicra.marketresearch.repository.MarketRawDataRepository
import com.github.nmicra.marketresearch.service.TradingPeriodService
import com.github.nmicra.marketresearch.service.model.LLHHModelProviderService
import com.github.nmicra.marketresearch.service.report.LLHHReportService
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.tribuo.Model
import org.tribuo.regression.Regressor
import kotlin.math.abs

@RestController
class PredictController {

    @Autowired
    lateinit var marketRawDataRepository: MarketRawDataRepository

    @Autowired
    lateinit var LLHHReportService: LLHHReportService

    @Autowired
    lateinit var LLHHPredictor: LLHHPredictor

    @Autowired
    lateinit var LLHHMVPredictor: LLHHMVPredictor

    @Autowired
    lateinit var LLHHDistancePredictor: LLHHDistancePredictor

    @Autowired
    lateinit var tradingPeriodService : TradingPeriodService

    @Autowired
    lateinit var llHHModelProviderService : LLHHModelProviderService

    @Autowired
    lateinit var timeSeriesPredictor : XGBoostTimeSeriesPredictor



    @PostMapping("/predict/timeSeries")
    suspend fun predictTimeSeries(@RequestBody query : TimeSeriesPredictionRequest) : List<Double>{
        return timeSeriesPredictor.predictWithCSV(query.dataList,query.timeUnitsForModelling,query.timeUnitsToPredict)
    }


    @GetMapping("/predict/llhh/{label}/{interval}")
    suspend fun predictLLHH(
        @PathVariable label: String,
        @PathVariable interval: String
    ): String {

//        // 1. Predict Classification

        val classificationModelLLHH = llHHModelProviderService.getLLHHClassificationModel(label, interval)
        val distanceModel = llHHModelProviderService.getLLHHDistanceModel(label, interval)

        val tradingData = runBlocking { tradingPeriodService.tradingPeriodByInterval(interval,label) }
        val classificationReport = LLHHReportService.createLLHHReportWithMV(tradingData, false)
            .filterNot { it.contains("-99999") }
            .map { it.split(",").drop(2).joinToString(",") }//drop first 2 columns which is date & closePrice
        val classificationQueryParams = classificationReport.last().split(",").map { it.removeSuffix("\r\n") }



        val classificationPrediction = LLHHMVPredictor.predictWithModel(classificationModelLLHH, classificationQueryParams)

        // 2. Predict Distance / Regression

        val distanceQueryParam = (classificationQueryParams + listOf<String>(classificationPrediction.output.label.toString())).map { it.removeSuffix("\r\n") }
        val distancePrediction = LLHHDistancePredictor.predictWithModel(distanceModel.first, distanceQueryParam, true)
        return "Classification -> ${classificationPrediction.output.label} : ${classificationPrediction.output.score}, Distance ->  ${distancePrediction.output?.values!![0]}"
    }
}

data class TimeSeriesPredictionRequest(val dataList: List<Double>, val timeUnitsForModelling : Int = 10, val timeUnitsToPredict : Int = 5)