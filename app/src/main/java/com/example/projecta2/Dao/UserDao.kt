package com.example.projecta2.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.projecta2.Entity.UserInfo

@Dao
interface UserDao {
    @Insert
    fun insertUser(userInfo : UserInfo)
    @Query("select * from UserInfo where email = :email")
    fun getUserInfoObj(email : String) : UserInfo?

    @Query("DELETE FROM UserInfo WHERE email = :email")
    fun deleteByEmail(email: String): Int

    @Query("select id from UserInfo where email = :email")
    fun getUserInfoId(email : String) : Long

    @Query("DELETE FROM UserInfo")
    fun deleteAllUsers()
}