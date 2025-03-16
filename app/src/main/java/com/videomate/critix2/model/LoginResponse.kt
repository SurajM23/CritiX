package com.videomate.critix2.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)