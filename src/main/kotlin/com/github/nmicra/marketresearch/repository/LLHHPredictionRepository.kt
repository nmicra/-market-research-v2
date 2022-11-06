package com.github.nmicra.marketresearch.repository

import com.github.nmicra.marketresearch.entity.LLHHPrediction
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LLHHPredictionRepository : CoroutineCrudRepository<LLHHPrediction,Long> {
}
