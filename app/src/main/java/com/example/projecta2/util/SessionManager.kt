package com.example.projecta2.View

import android.content.Context
import com.example.projecta2.model.User
import retrofit2.Callback


object SessionManager {
    private const val PREF_NAME = "MyAppPrefs"
    private const val KEY_USER_EMAIL = "user_email"

    fun saveUserEmail(context: Context, email: String) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply()
    }


    fun getUserEmail(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }


    fun clearSession(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
