package com.videomate.critix.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)