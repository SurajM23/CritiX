package com.videomate.critix.model

data class UpdateUserResponse(
    val status: Int,
    val message: String,
    val success: Boolean,
    val data: UserData
)

data class UserData3(
    val userId: String,
    val username: String,
    val email: String,
    val description: String,
    val profileImageUrl: String
)
