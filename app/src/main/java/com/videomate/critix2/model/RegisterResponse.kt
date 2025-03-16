package com.videomate.critix2.model

data class RegisterResponse(
    val status: Int,
    val data: RegisterData?,
    val message: String = "Something went wrong",
    val success: Boolean
)