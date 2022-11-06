package com.github.nmicra.marketresearch.service.model

import com.github.nmicra.marketresearch.ml.predictor.RegressionPrecision
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHDistancePredictor
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHMVPredictor
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHPredictor
import com.github.nmicra.marketresearch.service.TradingPeriodService
import com.github.nmicra.marketresearch.service.report.LLHHReportService
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.tribuo.Model
import org.tribuo.classification.Label
import org.tribuo.regression.Regressor
import kotlin.math.abs

@Service
class LLHHModelProviderService {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    val llhhClassificationModelMap = mutableMapOf<String, Model<Label>>()
    val llhhDistanceModelMap = mutableMapOf<String, Pair<Model<Regressor?>, RegressionPrecision>>()

    @Autowired
    lateinit var LLHHReportService: LLHHReportService

    @Autowired
    lateinit var LLHHPredictor: LLHHPredictor

    @Autowired
    lateinit var LLHHMVPredictor: LLHHMVPredictor

    @Autowired
    lateinit var LLHHDistancePredictor: LLHHDistancePredictor

    @Autowired
    lateinit var tradingPeriodService: TradingPeriodService

    fun getLLHHClassificationModel(label: String, interval: String): Model<Label> {
        return llhhClassificationModelMap.getOrPut("${label}_$interval") {
            val tradingData = runBlocking { tradingPeriodService.tradingPeriodByInterval(interval, label) }

            val classificationReport = LLHHReportService.createLLHHReportWithMV(tradingData, false)
                .filterNot { it.contains("-99999") }
                .map { it.split(",").drop(2).joinToString(",") }//drop first 2 columns which is date & closePrice

            LLHHMVPredictor.createClassificationModelLLHH(classificationReport.filterNot { it.contains("QUERY") })
        }
    }

    fun getLLHHDistanceModel(label: String, interval: String): Pair<Model<Regressor?>, RegressionPrecision> {
        return llhhDistanceModelMap.getOrPut("${label}_$interval") {
            val tradingData = runBlocking { tradingPeriodService.tradingPeriodByInterval(interval, label) }

            val distanceReport = LLHHReportService.createLLHHReportWithMV(tradingData, true)
                .map { it.split(",").drop(2).joinToString(",") }//drop first 2 columns which is date

            val distanceLst = mutableListOf<Pair<Model<Regressor?>, RegressionPrecision>>()
            for (i in 10..100 step 10) {
                distanceLst.add(
                    LLHHDistancePredictor.createLLHHDistanceModel(
                        distanceReport.filterNot { it.contains("QUERY") },
                        i, mvIncluded = true))
            }
            val distModel = distanceLst.reduce {model1,model2
                -> if ((model1.second.rmse+ abs(model1.second.r2))/2 < (model2.second.rmse+ abs(model2.second.r2))/2) model1 else model2}
            logger.debug(">>> the chosen distance model for [$label $interval] is ${distModel.second}")
            distModel
        }
    }

}