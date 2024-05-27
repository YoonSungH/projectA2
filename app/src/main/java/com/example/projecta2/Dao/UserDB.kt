package com.example.projecta2.Dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.projecta2.Entity.UserInfo

@Database(entities = [UserInfo::class], version = UserDB.DATABASE_VERSION)
@TypeConverters(Converters::class)
abstract class UserDB : RoomDatabase() {
    abstract fun getDao() : UserDao

    companion object {
        const val DATABASE_VERSION = 2
    }
}
