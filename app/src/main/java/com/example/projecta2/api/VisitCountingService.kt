package com.example.projecta2.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface VisitCountingService {

    @POST("/m_visit/myCounting")
    @FormUrlEncoded
    fun getMyCounting(@Field("userId") userId: Long): Call<ResponseBody>

}
