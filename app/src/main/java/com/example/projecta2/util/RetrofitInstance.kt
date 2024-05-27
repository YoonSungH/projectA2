package com.example.projecta2.util

import com.example.projecta2.api.GymService
import com.example.projecta2.api.ReservationService
import com.example.projecta2.api.ReviewService
import com.example.projecta2.api.UserService
import com.example.projecta2.api.VisitCountingService
import com.example.projecta2.model.User
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://10.100.103.27:8111/"

    private const val CONNECT_TIMEOUT_SECONDS = 30L // 연결 Timeout 시간 (초)
    private const val READ_TIMEOUT_SECONDS = 30L // 읽기 Timeout 시간 (초)
    private lateinit var authUser: User // 사용자 정보 저장용 변수

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        var gson = GsonBuilder().setLenient().create()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    //센터 조회
    val gymService: GymService by lazy {
        retrofit.create(GymService::class.java)
    }

    //유저 관련 Service
    val userService : UserService by lazy{
        retrofit.create(UserService::class.java)
    }

    //헬스장 예약 관련
    val reservationService : ReservationService by lazy{
        retrofit.create(ReservationService::class.java)
    }

    //리뷰 관련
    val reviewService : ReviewService by lazy{
        retrofit.create(ReviewService::class.java)
    }

    //예약 카운팅 관련
    val visitCountingService : VisitCountingService by lazy{
        retrofit.create(VisitCountingService::class.java)
    }


}