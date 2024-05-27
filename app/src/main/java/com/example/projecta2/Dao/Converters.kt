package com.example.projecta2.Dao

import androidx.room.TypeConverter
import java.util.Arrays

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return if (value == null) null else Arrays.asList(
            *value.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
    }

    @TypeConverter
    fun fromList(list: List<String?>?): String? {
        return if (list == null) null else java.lang.String.join(",", list)
    }
}

