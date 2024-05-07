package com.example.wondrobe.ui.user

import android.content.Context

object SharedPreferencesManager {
    private const val FOLLOW_STATE_PREFS = "follow_state_prefs"
    private const val CURRENT_USER_FOLLOWING_COUNT = "current_user_following_count"
    private const val SELECTED_USER_FOLLOWERS_COUNT = "selected_user_followers_count"

    fun saveFollowingState(context: Context, username: String?, isFollowing: Boolean) {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(username, isFollowing)
        editor.apply()

        // Incrementar el contador de following del usuario actual si sigue al usuario
        val currentUserFollowingCount = getFollowingCount(context) + if (isFollowing) 1 else 0
        saveFollowingCount(context, currentUserFollowingCount)
    }

    fun getFollowingState(context: Context, username: String?): Boolean {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(username, false) // Por defecto, no sigue
    }

    fun saveFollowingCount(context: Context, count: Int) {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(CURRENT_USER_FOLLOWING_COUNT, count)
        editor.apply()
    }

    fun getFollowingCount(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(CURRENT_USER_FOLLOWING_COUNT, 0)
    }

    fun saveFollowersCount(context: Context, username: String?, count: Int) {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(username + "_followers_count", count)
        editor.apply()
    }

    fun getFollowersCount(context: Context, username: String?): Int {
        val sharedPreferences = context.getSharedPreferences(FOLLOW_STATE_PREFS, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(username + "_followers_count", 0)
    }
}