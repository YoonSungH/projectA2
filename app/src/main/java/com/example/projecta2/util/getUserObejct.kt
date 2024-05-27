package com.example.projecta2.util

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.projecta2.Dao.UserDB
import com.example.projecta2.Entity.UserInfo
import com.example.projecta2.View.DatabaseInitializer
import com.example.projecta2.View.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//세션에서 저장된 로그인 된 유저의 email을 가지고 내장DB에 사용된 정보 가져오는 클래스 및 함수
class getUserObject(private val context: Context) {

    // Room Database instance
    private val db: UserDB by lazy {
        DatabaseInitializer.initDatabase(context)
    }

    suspend fun getUserInfo(): UserInfo? {
        val email = SessionManager.getUserEmail(context)
        if (email != null) {
            Log.d("Email Log", email)
            return withContext(Dispatchers.IO) {
                db.getDao().getUserInfoObj(email)
            }
        } else {
            Log.d("Email Log", "Email is null")
            return null
        }
    }
}
