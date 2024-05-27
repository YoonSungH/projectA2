package com.example.projecta2.api

import com.example.projecta2.model.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserService {

    //로그인 처리
    @FormUrlEncoded
    @POST("/loginPro")
    fun signIn(@Field("email") email: String, @Field("password") password: String): Call<User>

    //회원가입
    @POST("/m_user/join")
    fun join(@Body user: User): Call<ResponseBody>

    //회원가입 => 아이디 중복검사(email)
    @FormUrlEncoded
    @POST("/m_user/inquiryEmail")
    fun inquiryEmail(@Field("email")email : String) : Call<Void>


    //회원정보 수정
    @POST("/m_user/update")
    fun userUpdate(@Body updatedUser: User): Call<ResponseBody>



    // 회원 정보 조회
    @POST("/m_user/user-info")
    @FormUrlEncoded
    fun getUserInfo(@Field("email") email: String): Call<User>

    // 회원 삭제
    @DELETE("/m_user/deleteUser")
    fun deleteUser(@Query("email") email: String): Call<ResponseBody>

}