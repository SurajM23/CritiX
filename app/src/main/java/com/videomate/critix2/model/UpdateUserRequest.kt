package com.videomate.critix2.model

data class UpdateUserRequest(
    val username: String,
    val email: String,
    val description: String
)
