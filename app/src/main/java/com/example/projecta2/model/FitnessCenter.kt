package com.example.projecta2.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class FitnessCenter(

    val id: Long =0 ,

    val name: String,
    val dailyPassPrice: Long,
    var distance: Float? = 0f,
    val address: String,
    val imagePath: String?, // 이미지 경로 변수 추가
    val latitude: Double,
    val longitude: Double
) {
    constructor(id: Long) : this(id, "", 0, 0.0f, "", null, 0.0, 0.0)

}

