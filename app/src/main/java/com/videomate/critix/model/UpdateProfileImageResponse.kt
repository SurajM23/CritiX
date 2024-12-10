package com.videomate.critix.model

data class UpdateProfileImageResponse(
    val status: Int,
    val message: String,
    val success: Boolean,
    val data: UserData?
)

data class UserData2(
    val username: String,
    val profileImageUrl: String
)