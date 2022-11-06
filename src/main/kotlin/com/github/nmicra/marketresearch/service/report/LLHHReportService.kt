package com.github.nmicra.marketresearch.service.report

import com.github.nmicra.marketresearch.analysis.*
import com.github.nmicra.marketresearch.general.BIGDECIMAL_SCALE
import kotlinx.datetime.toJavaLocalDate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class LLHHReportService {


    fun createLLHHReportWithMV(rawData : List<TradingPeriod>, includeNextLLHHDistance : Boolean = true) : List<String> {
        rawData.calculateMA(5)
        rawData.calculateMA(7)
        val workingData = rawData.filter { it.movingAvg.contains(7) }
        val resultsList = mutableListOf<String>()
        val criticals = identifyAndLabelCriticals2(workingData.map { Pair(it.close.toDouble(), it.startingDate.toJavaLocalDate()) })
            .filterNot { it.date == null }
            .sortedBy { it.index }
        var ind = firstIndexContainsAllHHLLs(criticals) + 1

        val lastCriticalsIndex = criticals.maxOf { it.index }/*NOTE do not use criticals.lastIndex */
        while (ind <= workingData.lastIndex) {
            val criticalDate : LocalDate = when {
                ind > lastCriticalsIndex -> workingData[ind].startingDate.toJavaLocalDate()
                else -> criticals.firstOrNull { it.index == ind }?.date ?: run {
                    val lastKnownIndex = criticals.filter { it.index < ind }.maxOf { it.index }
                    val delta = (ind - lastKnownIndex).toLong()
                    val calculatedDate = when(workingData[0]){
                        is TradingDay -> criticals.first { it.index==lastKnownIndex }.date!!.plusDays(delta)
                        is TradingWeek -> {
                            val dt = criticals.first { it.index==lastKnownIndex }.date!!
                            when {
                                dt.dayOfWeek.value in 2..5 -> dt.plusWeeks(delta).minusDays((dt.dayOfWeek.value-1).toLong()) /*not monday && not weekend*/
                                else -> dt.plusWeeks(delta)
                            }
                        }
                        is TradingMonth -> {
                            val dt = criticals.first { it.index==lastKnownIndex }.date!!.plusMonths(delta)
                            LocalDate.of(dt.year,dt.month,1)
                        }
                        is TradingYear -> {
                            val dt = criticals.first { it.index==lastKnownIndex }.date!!.plusYears(delta)
                            LocalDate.of(dt.year,1,2) // 2-of Jan
                        }
                    }
                    return@run when (calculatedDate.dayOfWeek.value){
                        6 -> calculatedDate.plusDays(2) // When Saturday, add 2 days
                        7 -> calculatedDate.plusDays(1) // When Sunday, add 1 days
                        else -> calculatedDate
                    }
                }
            }


            val rawTradingPeriod = workingData.firstOrNull { it.startingDate.toJavaLocalDate() == criticalDate } ?: workingData.filter { it.startingDate.toJavaLocalDate().isAfter(criticalDate) }[0]
            val mv5Dev = rawTradingPeriod.close.divide(rawTradingPeriod.movingAvg[5], BIGDECIMAL_SCALE, RoundingMode.HALF_UP)
            val mv7Dev = rawTradingPeriod.close.divide(rawTradingPeriod.movingAvg[7], BIGDECIMAL_SCALE, RoundingMode.HALF_UP)

            val currentIndexClassification = criticals.filter { it.index == ind }
                .map { it.hhll.toString() }.firstOrNull()
                ?: "NONE"
            val distanceToHH = ind - criticals.filter { it.index <= ind }
                .filter { it.hhll == HHLLClassification.HH }
                .maxOf { it.index }
            val distanceToLL = ind - criticals.filter { it.index <= ind }
                .filter { it.hhll == HHLLClassification.LL }
                .maxOf { it.index }

            val distanceToLH = ind - criticals.filter { it.index <= ind }
                .filter { it.hhll == HHLLClassification.LH }
                .maxOf { it.index }

            val distanceToHL = ind - criticals.filter { it.index <= ind }
                .filter { it.hhll == HHLLClassification.HL }
                .maxOf { it.index }

            val (nextLLHHClassification,nextLLHHDistance)= when {
                ind >= lastCriticalsIndex -> Pair(HHLLClassification.QUERY, -999)
                else -> {
                    val nextLLHH = criticals.first { it.index > ind }
                    Pair(nextLLHH.hhll, nextLLHH.index - ind)
                }
            }


            when(includeNextLLHHDistance){
                true -> resultsList.add("${criticalDate},${rawTradingPeriod.close},${mv5Dev},${mv7Dev},$currentIndexClassification,$distanceToHH,$distanceToLL,$distanceToLH,$distanceToHL,$nextLLHHClassification,$nextLLHHDistance\r\n")
                else -> resultsList.add("${criticalDate},${rawTradingPeriod.close},${mv5Dev},${mv7Dev},$currentIndexClassification,$distanceToHH,$distanceToLL,$distanceToLH,$distanceToHL,$nextLLHHClassification\r\n")
            }

            ind++
        }
        return resultsList
    }

    private fun identifyAndLabelCriticals2(closePriceParam: List<Pair<Double,LocalDate>>) : List<TradingHHLLClassification> {
        check(closePriceParam.size > 10) {"list is too shrt"}
        val lst = closePriceParam.map { it.toHHLL() }
        val closePrice = closePriceParam.map { it.first }

        val criticals  = when { // initial setup of first 2 nodes, either (HH,HL or LL,LH)
            closePrice[0] > closePrice[1] -> mutableListOf(TradingHHLLClassification(closePrice[0],0,HHLLClassification.HH), TradingHHLLClassification(closePrice[1],1,HHLLClassification.HL))
            else -> mutableListOf(TradingHHLLClassification(closePrice[0],0,HHLLClassification.LL), TradingHHLLClassification(closePrice[1],1,HHLLClassification.LH))
        }

        for ((ind,trading) in lst.drop(2).withIndex() ){
            when(criticals.last().hhll){
                HHLLClassification.HL -> {
                    if (trading.close < criticals.last().close){
                        criticals.add(trading.copy(hhll = HHLLClassification.LL, index = ind+2/* drop 2 in the loop*/))
                    } else if(trading.close > criticals[criticals.lastIndex-1].close) {
                        criticals.add(trading.copy(hhll = HHLLClassification.LH, index = ind+2/* drop 2 in the loop*/))
                    }
                }
                HHLLClassification.LH -> {
                    if (trading.close > criticals.last().close){
                        criticals.add(trading.copy(hhll = HHLLClassification.HH, index = ind+2/* drop 2 in the loop*/))
                    } else if(trading.close < criticals[criticals.lastIndex-1].close) {
                        criticals.add(trading.copy(hhll = HHLLClassification.HL, index = ind+2/* drop 2 in the loop*/))
                    }
                }
                HHLLClassification.LL -> {
                    if (trading.close < criticals.last().close){
                        criticals.add(trading.copy(hhll = HHLLClassification.LL, index = ind+2/* drop 2 in the loop*/))
                    } else if(trading.close > criticals[criticals.lastIndex-1].close) {
                        criticals.add(trading.copy(hhll = HHLLClassification.LH, index = ind+2/* drop 2 in the loop*/))
                    }
                }
                HHLLClassification.HH -> {
                    if (trading.close > criticals.last().close){
                        criticals.add(trading.copy(hhll = HHLLClassification.HH, index = ind+2/* drop 2 in the loop*/))
                    } else if(trading.close < criticals[criticals.lastIndex-1].close) {
                        criticals.add(trading.copy(hhll = HHLLClassification.HL, index = ind+2/* drop 2 in the loop*/))
                    }
                }
                else -> error("not supported state $trading")
            }
        }
        return criticals
    }



    private fun firstIndexContainsAllHHLLs(criticalsLst: List<TradingHHLLClassification>): Int {
        check(criticalsLst.size >= 4) {"list is too short to contain all HH,HL,LL,LH values"}
        val criticlasMapped =  criticalsLst.map { it.hhll }
        var ind = 3
        while (ind < criticlasMapped.size) {
            if (criticlasMapped.subList(0,ind).containsAll(listOf(
                    HHLLClassification.HH,
                    HHLLClassification.HL, HHLLClassification.LL, HHLLClassification.LH))){
                return criticalsLst[ind].index
            }
            ind++
        }
        error("index not found")
    }
}


enum class HHLLClassification{HH,HL,LL,LH,NONE,QUERY}
data class TradingHHLLClassification(val close : Double, var index : Int = -1, var hhll : HHLLClassification = HHLLClassification.NONE, var date: LocalDate? = null)
fun Double.toHHLL() : TradingHHLLClassification = TradingHHLLClassification(this)
fun Pair<Double,LocalDate>.toHHLL() : TradingHHLLClassification = TradingHHLLClassification(close = this.first, date = this.second)