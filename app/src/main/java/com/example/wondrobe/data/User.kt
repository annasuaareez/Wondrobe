package com.example.wondrobe.data

data class User(
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val biography: String? = null,
    val password: String? = null,
    val profileImage: String? = null,
    val bannerImage: String? = null,
    val isAdmin: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)
