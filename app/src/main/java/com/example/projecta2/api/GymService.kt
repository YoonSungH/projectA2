package com.example.projecta2.api

import com.example.projecta2.model.FitnessCenter
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface GymService {
    @GET("/m_centerManage/gymList")
    fun getGymList(): Call<List<FitnessCenter>>

}