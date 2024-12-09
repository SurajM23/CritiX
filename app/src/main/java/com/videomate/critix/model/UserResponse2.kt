package com.videomate.critix.model

data class UserResponse2(
    val status: Int,
    val data: List<User>
)

data class User(
    val id: String,
    val username: String,
    val profileImageUrl: String?
)
