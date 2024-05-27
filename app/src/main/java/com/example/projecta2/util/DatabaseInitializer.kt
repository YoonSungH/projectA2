package com.example.projecta2.View

import android.content.Context
import androidx.room.Room
import com.example.projecta2.Dao.UserDB

class DatabaseInitializer private constructor() {

    companion object {
        @Volatile private var INSTANCE: UserDB? = null

        fun initDatabase(context: Context): UserDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDB::class.java, "UserDB"
                )
                    .fallbackToDestructiveMigration() // 스키마 변경 시 데이터 손실 가능성이 있는 옵션
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
