package com.github.nmicra.marketresearch.controller

import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHDistancePredictor
import com.github.nmicra.marketresearch.ml.predictor.llhh.LLHHMVPredictor
import com.github.nmicra.marketresearch.service.model.LLHHModelProviderService
import com.github.nmicra.marketresearch.service.report.LLHHReportService
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ModelsQueryController {

    @Autowired
    lateinit var llHHModelProviderService : LLHHModelProviderService

    @Autowired
    lateinit var LLHHMVPredictor: LLHHMVPredictor

    @Autowired
    lateinit var LLHHDistancePredictor: LLHHDistancePredictor


    @PostMapping("/query/llhh")
    suspend fun queryLLHH(
        @RequestBody query : LLHHQueryRequest
    ): String {
        val classificationModelLLHH = llHHModelProviderService.getLLHHClassificationModel(query.label, query.interval)
        val distanceModel = llHHModelProviderService.getLLHHDistanceModel(query.label, query.interval)

        val queryList = listOf("${query.mv5Dev}", "${query.mv7Dev}", "${query.currentIndexClassification}", "${query.distanceToHH}", "${query.distanceToLL}", "${query.distanceToLH}", "${query.distanceToHL}")
        val classificationPrediction = LLHHMVPredictor.predictWithModel(classificationModelLLHH, queryList)


        val distanceQueryParam = (queryList + listOf<String>(classificationPrediction.output.label.toString())).map { it.removeSuffix("\r\n") }
        val distancePrediction = LLHHDistancePredictor.predictWithModel(distanceModel.first, distanceQueryParam, true)
        return "Classification -> ${classificationPrediction.output.label} : ${classificationPrediction.output.score}, Distance ->  ${distancePrediction.output?.values!![0]}"
    }
}

data class LLHHQueryRequest(val label: String, val interval: String,val mv5Dev : Double, val mv7Dev : Double, val currentIndexClassification :String, val distanceToHH : Int, val distanceToLL : Int, val distanceToLH : Int, val distanceToHL : Int)