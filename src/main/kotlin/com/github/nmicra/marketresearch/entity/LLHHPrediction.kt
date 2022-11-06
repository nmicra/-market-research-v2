package com.github.nmicra.marketresearch.entity

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import org.jetbrains.kotlinx.dataframe.annotations.ColumnName
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate


@Table("llhh_prediction")
data class LLHHPrediction (

    @Id
    var id: Long? = null,
    var label: String = "",

    @JsonSerialize(using = LocalDateSerializer::class)
    @JsonDeserialize(using = LocalDateDeserializer::class)
    val date: LocalDate,
    val interval: String,
    val close: Double,
    @Column("mv5dev")
    val mv5Dev: Double,
    @Column("mv7dev")
    val mv7Dev: Double,
    val currentIndexClassification: String,
    @Column("distance2hh")
    val distanceToHH: Int,
    @Column("distance2ll")
    val distanceToLL: Int,
    @Column("distance2lh")
    val distanceToLH: Int,
    @Column("distance2hl")
    val distanceToHL: Int,
    @Column("next_llhh")
    val nextLLHH: String,
    @Column("next_llhh_distance")
    val nextLLHHDistance: Int
)