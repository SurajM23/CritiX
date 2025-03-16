package com.videomate.critix2.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)