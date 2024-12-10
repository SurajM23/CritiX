package com.videomate.critix.model

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val user: User
)

data class User2(
    val username: String,
    val email: String,
    val description: String,
    val profileImageUrl: String
)
