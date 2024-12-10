package com.videomate.critix.model

data class UpdateUserRequest(
    val username: String,
    val email: String,
    val description: String
)
