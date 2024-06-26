package com.example.wondrobe.utils

import android.content.Context
import com.example.wondrobe.data.User
import com.google.gson.Gson

object SharedPreferencesManager {
    private const val FOLLOW_STATE_PREFS = "follow_state_prefs"
    private const val FOLLOWERS_COUNT_PREFS = "followers_count_prefs"
    private const val FOLLOWING_COUNT_PREFS = "following_count_prefs"
    private const val USER_ID_KEY = "user_id_key"

    // Guarda el estado de seguimiento
    fun saveFollowingState(context: Context, userId: String, isFollowing: Boolean) {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(userId, isFollowing)
        editor.apply()
    }

    // Recupera el estado de seguimiento
    fun getFollowingState(context: Context, userId: String): Boolean {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(userId, false) // Por defecto, no sigue
    }

    // Guarda el conteo de seguidores
    fun saveFollowersCount(context: Context, userId: String, count: Int) {
        val sharedPreferences = context.getSharedPreferences(FOLLOWERS_COUNT_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(userId, count)
        editor.apply()
    }

    // Recupera el conteo de seguidores
    fun getFollowersCount(context: Context, userId: String): Int {
        val sharedPreferences = context.getSharedPreferences(FOLLOWERS_COUNT_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(userId, 0)
    }

    // Guarda el conteo de seguidos
    fun saveFollowingCount(context: Context, userId: String, count: Int) {
        val sharedPreferences = context.getSharedPreferences(FOLLOWING_COUNT_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(userId, count)
        editor.apply()
    }

    // Recupera el conteo de seguidos
    fun getFollowingCount(context: Context, userId: String): Int {
        val sharedPreferences = context.getSharedPreferences(FOLLOWING_COUNT_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(userId, 0)
    }

    private const val PREF_CURRENT_USER = "current_user"

    // Recupera el usuario actual
    fun getCurrentUser(context: Context): User? {
        val sharedPreferences = context.getSharedPreferences(PREF_CURRENT_USER, Context.MODE_PRIVATE)
        val userJson = sharedPreferences.getString(PREF_CURRENT_USER, null)
        return Gson().fromJson(userJson, User::class.java)
    }

    // Recupera el ID del usuario actual
    fun getUserId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPreferences.getString(USER_ID_KEY, null)
    }
}
