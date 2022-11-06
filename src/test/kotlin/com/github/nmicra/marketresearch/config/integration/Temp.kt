package com.github.nmicra.marketresearch.integration

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.ColumnName
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.filterBy
import org.jetbrains.kotlinx.dataframe.api.head
import org.jetbrains.kotlinx.dataframe.api.toListOf
import org.jetbrains.kotlinx.dataframe.io.read
import org.jetbrains.kotlinx.dataframe.size
import org.junit.jupiter.api.Test

import java.io.File
//import java.time.LocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus


class Temp {


//    @Test
//    fun testtemppp(){
//        val lst = listOf<Double>(35.43 ,35.880001 ,35.25 ,35.380001 ,36.970001 ,36.380001 ,33.990002 ,36.040001 ,36.119999 ,35.650002 ,36.299999 ,37.529999 ,40.169998 ,40.040001 ,40.790001 ,37.900002 ,37.73 ,37.799999 ,39.990002 ,38.209999 ,37.43 ,37.830002 ,38.610001 ,40.34 ,41.439999 ,42.490002 ,42.580002 ,44.709999 ,43.75 ,42.580002 ,45.68 ,44.599998 ,42.700001 ,41.470001 ,40.09 ,40.59 ,41.720001 ,41.880001 ,41.91 ,42.130001 ,42.84 ,45.779999 ,44.5 ,43.369999 ,43.610001 ,44.689999 ,47.5 ,48.029999 ,50.740002 ,52.189999 ,52.009998 ,49.919998 ,51.57 ,48.029999 ,50.799999 ,50.07 ,50.169998 ,49.759998 ,46.91 ,45.689999 ,45.130001 ,46.18 ,46.380001 ,43.470001 ,45.650002 ,46.25 ,48.959999 ,48.119999 ,45.799999 ,43.970001 ,44.110001 ,43.099998 ,44.009998 ,44.32 ,43.619999 ,46.860001 ,46.830002 ,43.959999 ,44.970001 ,41.220001 ,39.880001 ,41.509998 ,42.169998 ,44.799999 ,45.830002 ,44.029999 ,41.110001 ,40.5 ,38.799999 ,36.98 ,39.419998 ,36.990002 ,36.580002 ,40.110001 ,43.200001 ,45.990002 ,44.779999 ,43.490002 ,43.16 ,45.630001 ,42.5 ,44.09 ,45.41 ,42.630001 ,41.029999 ,39.209999 ,42.139999 ,42.41 ,43.029999 ,41.169998 ,43.080002 ,41.02 ,43.599998 ,38.990002 ,36.93 ,41.080002 ,41.130001 ,45.630001 ,47.75 ,52.43 ,49.869999 ,50.130001 ,47.130001 ,48.869999 ,49.560001 ,50.68 ,54.349998 ,52.459999 ,53.34 ,56.130001 ,59.669998 ,57.349998 ,58.990002 ,61.73 ,59.84 ,59.84 ,60.650002 ,62.66 ,63.400002 ,66.480003 ,70.5 ,67.540001 ,66.290001 ,68.68 ,71.389999 ,67.059998 ,64.510002 ,66.400002 ,65.669998 ,66.970001 ,63.98 ,65.769997 ,62.93 ,60.040001 ,54.389999 ,52.290001 ,55.580002 ,59.540001 ,61.709999 ,58.619999 ,58.049999 ,60.23 ,63.259998 ,67.559998 ,68.309998 ,70.470001 ,67.82 ,65.620003 ,60.880001 ,63.389999 ,64.800003 ,68.18 ,72.870003 ,75.279999 ,71.389999 ,71.900002 ,73.760002 ,76.110001 ,72.389999 ,72.480003 ,72.75 ,69.040001 ,73.139999 ,77.080002 ,75.43 ,68.910004 ,66.330002 ,69.029999 ,70.800003 ,73.540001 ,71.519997 ,75.870003 ,75.75 ,76.910004 ,80.239998 ,79.980003 ,84.559998 ,86.980003 ,84.639999 ,84.419998 ,85.580002 ,86.120003 ,92.690002 ,96.989998 ,94.589996 ,96.699997 ,93.830002 ,95.629997 ,97.410004 ,99.07 ,98.260002 ,98.370003 ,94.730003 ,97.199997 ,91.870003 ,95.43 ,93.419998 ,94.5 ,96.169998 ,97.730003 ,103.220001 ,100.110001 ,94.949997 ,93.529999 ,99.019997 ,98.599998 ,105.690002 ,106.919998 ,107.120003 ,107.769997 ,105.959999 ,108.489998 ,113.239998 ,113.82 ,116.720001 ,118.919998 ,117.529999 ,118.489998 ,117.339996 ,116.540001 ,120.57 ,123.400002 ,122.290001 ,124.389999 ,124.639999 ,123.089996 ,125.120003 ,121.279999 ,121.769997 ,118.68 ,121.199997 ,121.809998 ,117.75 ,119.660004 ,118.699997 ,119.599998 ,117.050003 ,115.800003 ,115.580002 ,113.43 ,111.739998 ,110.239998 ,110.599998 ,112.599998 ,109.940002 ,109.040001 ,107.190002 ,111.169998 ,110.529999 ,109.720001 ,112.139999 ,117.029999 ,117.129997 ,119.339996 ,118.110001 ,117.139999 ,116.139999 ,121.5 ,118.57 ,117.82 ,116.860001 ,118.169998 ,120.459999 ,121.709999 ,121.169998 ,124.709999 ,125.07 ,124.830002 ,123.720001 ,122.029999 ,122.790001 ,122.029999 ,119.160004 ,121.029999 ,121.059998 ,119.480003 ,115.800003 ,113.849998 ,115.839996 ,115.889999 ,116.980003 ,120.099998 ,121.849998 ,121.300003 ,121.440002 ,124.699997 ,123.010002 ,125.57 ,123.279999 ,120.220001 ,121.120003 ,120.0 ,121.599998 ,121.349998 ,118.620003 ,121.599998 ,122.43 ,121.709999 ,122.660004 ,120.760002 ,117.279999 ,116.529999 ,116.400002 ,118.169998 ,122.269997 ,124.739998 ,125.620003 ,123.309998 ,124.260002 ,127.239998 ,128.149994 ,129.160004 ,130.779999 ,130.880005 ,130.229996 ,125.489998 ,125.279999 ,123.529999 ,121.019997 ,119.510002 ,118.900002 ,118.5 ,114.529999 ,115.089996 ,118.330002 ,116.120003 ,114.970001 ,112.849998 ,113.129997 ,112.690002 ,109.510002 ,107.43 ,111.300003 ,111.25 ,112.099998 ,112.279999 ,111.43 ,109.379997 ,108.730003 ,105.839996 ,106.589996 ,102.970001 ,104.790001 ,103.309998 ,104.309998 ,99.480003 ,102.160004 ,106.120003 ,103.980003 ,109.720001 ,108.339996 ,111.550003 ,113.290001 ,116.910004 ,120.769997 ,121.760002 ,125.379997 ,126.239998 ,127.730003 ,123.699997 ,121.410004 ,121.82 ,119.0 ,120.440002 ,124.379997 ,126.620003 ,124.870003 ,127.559998 ,122.470001 ,123.260002 ,124.139999 ,120.82 ,123.830002 ,121.639999 ,120.849998 ,119.949997 ,114.419998 ,111.169998 ,113.970001 ,114.419998 ,114.779999 ,121.699997 ,124.720001 ,122.360001 ,120.07 ,127.459999 ,126.019997 ,128.880005 ,126.68 ,128.679993 ,121.190002 ,121.75 ,110.260002 ,117.07 ,118.43 ,125.110001 ,133.509995 ,136.699997 ,130.360001 ,129.509995 ,138.320007 ,139.039993 ,143.779999 ,152.610001 ,149.190002 ,152.169998 ,155.119995 ,156.580002 ,154.570007 ,153.100006 ,154.929993 ,153.309998 ,149.460007 ,148.350006 ,146.110001 ,146.889999 ,142.619995 ,137.440002 ,140.369995 ,138.350006 ,141.830002 ,146.669998 ,146.080002 ,144.809998 ,147.110001 ,146.130005 ,141.910004 ,145.279999 ,141.720001 ,140.990005 ,138.220001 ,142.479996 ,138.380005 ,128.229996 ,127.760002 ,124.690002 ,124.489998 ,127.099998 ,124.32 ,129.800003 ,133.029999 ,134.960007 ,135.050003 ,131.339996 ,127.300003 ,126.07 ,123.949997 ,124.300003 ,124.25 ,123.870003 ,125.199997 ,119.980003 ,122.169998 ,120.68 ,117.190002 ,114.279999 ,112.339996 ,112.760002 ,112.370003 ,111.779999 ,108.480003 ,106.139999 ,107.419998 ,104.900002 ,103.260002 ,101.019997 ,101.230003 ,99.540001 ,99.400002 ,98.889999 ,98.730003 ,94.760002 ,97.43 ,102.08 ,100.82)
//        println(">>> ${lst.reversed()}")
//    }

//    @Test
//    fun test(){
//        val from = JavaLocalDate.of(2022,1,1).toEpochDay() * 24 *60 *60
//        val to = JavaLocalDate.now().toEpochDay() * 24 *60 *60
//        println("from=$from, to=$to")
//        val client = OkHttpClient()
//        val request = Request.Builder().url("https://query1.finance.yahoo.com/v7/finance/download/GOLD?period1=${from}&period2=${to}&interval=1d&events=history")
//            .addHeader("Accept", "*/*")
//            .addHeader("cache-Control", "no-cache")
//            .addHeader("connection", "keep-alive")
//            .addHeader("accept-encoding", "gzip,csv,deflate")
//            .addHeader("Content-Type", "application/octet-stream")
//            .addHeader("Content-Transfer-Encoding", "Binary")
//            .build()
//        val response =client.newCall(request).execute()
//        File("kuku.csv").writeBytes(response.body()!!.byteStream().readAllBytes())
//        response.body()?.close()
//    }

    @Test
    fun test2(){
        val df = DataFrame.read("kuku.csv")
//            .filter { it.get(Day::date).isAfter(LocalDate.of(2022,2,2)) }
        println(df.columnNames())
        println(df.columnTypes())
        println(df.size())
        println(df.head(5))

        df.filterBy("Date")
    }

    @Test
    fun test3(){
        val df = DataFrame.read("kuku.csv")
            .filter { it.get(Day::date).minus(LocalDate(2022,2,10)).days >0 }
            .toListOf<Day>()
        println(">>> ${df[0]}")
    }


    @Test
    fun test4(){
        /*
HH -> HH
HH -> LH
LH -> LL
LH -> HL
LL -> LL
LL -> HL
HL -> HH
HL -> LH
         */
        // Int in criticals is the DISTANCE to previous critical
       val criticals  = mutableListOf(TradingHHLLClassification(3.5,0,HHLLClassification.HH), TradingHHLLClassification(2.3,1,HHLLClassification.HL))

        val lst = listOf<Double>(3.5,2.3,3.4,3.3,3.6,3.4,3.3,5.1,4.3,4.5,3.3,3.0,3.12,3.15,3.1,2.9,2.8,2.7).map { it.toHHLL() }
        for ((ind,trading) in lst.drop(2).withIndex() ){
//            println("ind=$ind trd=$trading")
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
        criticals.subList(0,3).map { it.hhll }

        println("criticals are: $criticals")
        println("first index contains all: ${firstIndexContainsAllHHLLs(criticals)}")
    }


    fun Double.toHHLL() : TradingHHLLClassification = TradingHHLLClassification(this)

    // returns first index, which contains all HH,HL,LL,LH

      fun firstIndexContainsAllHHLLs(criticalsLst: List<TradingHHLLClassification>): Int{
//    fun firstIndexContainsAllHHLLs(criticalsLst : List<HHLLClassification>) : Int {
        check(criticalsLst.size >= 4) {"list is too short to contain all HH,HL,LL,LH values"}
        val criticlasMapped =  criticalsLst.map { it.hhll }
        var ind = 3
        while (ind < criticlasMapped.size) {
            if (criticlasMapped.subList(0,ind).containsAll(listOf(
                    HHLLClassification.HH,
                    HHLLClassification.HL, HHLLClassification.LL, HHLLClassification.LH))){
//                return criticalsLst.subList(0,ind).map { it.index }.sum()
                return criticalsLst[ind].index
            }
            ind++
        }
        error("index not found")
    }
}
//TODO move over list, make csv currentStatus(NONE,LL,LH), numPrevLL,numPrevHH,numPrevHL,numPrevLH, Y=>nextLLorHH
//TODO move over list, make csv currentStatus(NONE,LL,LH), numPrevLL,numPrevHH,numPrevHL,numPrevLH, nextLLorHH, Y=>distanceTo
enum class HHLLClassification{HH,HL,LL,LH,NONE}
data class TradingHHLLClassification(val close : Double, var index : Int = -1, var hhll : HHLLClassification = HHLLClassification.NONE)


data class Day(
    @ColumnName("Date") val date: LocalDate,
    @ColumnName("Open") val open: Double,
    @ColumnName("High") val high: Double,
    @ColumnName("Low") val low: Double,
    @ColumnName("Close") val close: Double,
    @ColumnName("Adj Close") val adj: Double,
    val Volume: Long
)

/*
val passengers = DataFrame.read("titanic.csv")
    .filter { it.get(Passenger::city).endsWith("NY") }
    .toListOf<Passenger>()
 */