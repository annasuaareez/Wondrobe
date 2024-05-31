package com.example.wondrobe.utils

import android.content.Context
import android.content.SharedPreferences

object UserUtils {

    private const val PREFERENCES_FILE_KEY = "com.example.wondrobe.PREFERENCE_FILE_KEY"
    private const val USERNAME_KEY = "username"
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

    fun saveUsername(context: Context, username: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(USERNAME_KEY, username)
            apply()
        }
    }

    fun getUsername(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
        return sharedPreferences.getString(USERNAME_KEY, null)
    }

}

