package com.videomate.critix.model

data class LikeReviewResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: LikeReviewData
)

data class LikeReviewData(
    val reviewId: String,
    val totalLikes: Int,
    val liked: Boolean
)
