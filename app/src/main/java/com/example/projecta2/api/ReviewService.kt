package com.example.projecta2.api

import com.example.projecta2.model.Review
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReviewService {

    @POST("/m_review/add")
    fun addReview(@Body review: Review): Call<ResponseBody>

    // 모든 리뷰 조회
    @GET("/m_review/all/{centerId}")
    fun getAllReviews(@Path("centerId") centerId: Long): Call<List<Review>>

    //    @DELETE("/m_review/delete/{id}")
//    fun deleteReview(@Path("id") id: Long): Call<ResponseBody>
//    @DELETE("/m_review/delete")
//    fun deleteReview(@Body reviewDTO: ReviewDTO): Call<ResponseBody>
    @DELETE("/m_review/delete/{reviewId}/{userId}")
    fun deleteReview(@Path("reviewId") reviewId: Long, @Path("userId") userId: Long): Call<ResponseBody>

}