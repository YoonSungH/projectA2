package com.example.projecta2.model

import kotlinx.serialization.Serializable

@Serializable
data class Result<T>(
    val data: List<T>,
    val count: Int
)
