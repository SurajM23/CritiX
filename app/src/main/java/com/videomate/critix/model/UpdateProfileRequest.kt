package com.videomate.critix.model

import okhttp3.MultipartBody

data class UpdateProfileRequest(
    val username: String,
    val email: String,
    val description: String,
    val profileImage: MultipartBody.Part? = null
)