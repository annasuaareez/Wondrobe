package com.example.wondrobe.utils

import android.content.Context
import android.util.Log

object SaveManager {
    private const val PREFS_NAME = "WondrobePrefs"
    private const val SAVED_POSTS_KEY = "saved_posts"

    fun savePost(context: Context, postId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPosts = prefs.getStringSet(SAVED_POSTS_KEY, mutableSetOf()) ?: mutableSetOf()
        savedPosts.add(postId)
        prefs.edit().putStringSet(SAVED_POSTS_KEY, savedPosts).apply()
        Log.d("PreferencesManager", "Post saved: $postId")
    }

    fun removePost(context: Context, postId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPosts = prefs.getStringSet(SAVED_POSTS_KEY, mutableSetOf()) ?: mutableSetOf()
        savedPosts.remove(postId)
        prefs.edit().putStringSet(SAVED_POSTS_KEY, savedPosts).apply()
        Log.d("PreferencesManager", "Post removed: $postId")
    }

    fun isPostSaved(context: Context, postId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPosts = prefs.getStringSet(SAVED_POSTS_KEY, mutableSetOf()) ?: mutableSetOf()
        val isSaved = savedPosts.contains(postId)
        Log.d("PreferencesManager", "Post $postId is saved: $isSaved")
        return isSaved
    }
}
