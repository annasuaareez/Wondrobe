package com.example.wondrobe.utils

import android.content.Context
import android.content.SharedPreferences

object UserUtils {

    private const val USER_ID_KEY = "user_id"

    fun saveUserId(context: Context, userId: String) {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USER_ID_KEY, userId)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getString(USER_ID_KEY, null)
    }
}

