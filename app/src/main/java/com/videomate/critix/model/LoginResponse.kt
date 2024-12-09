package com.videomate.critix.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)