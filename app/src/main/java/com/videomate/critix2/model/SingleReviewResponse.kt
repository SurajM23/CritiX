package com.videomate.critix2.model

data class SingleReviewResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: ReviewDetails
)

data class ReviewDetails(
    val id: String,
    val movieTitle: String,
    val reviewText: String,
    val rating: Int,
    val author: AuthorInfo,
    val tags: List<String>,
    val likes: List<String>,
    val isLiked: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class AuthorInfo(
    val id: String,
    val username: String,
    val profileImageUrl: String?
)
