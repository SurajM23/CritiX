package com.videomate.critix.model

data class ReviewResponse(
    val status: Int,
    val message: String,
    val data: ReviewData
)

data class ReviewData(
    val review: Review
)

data class Review(
    val _id: String,
    val movieTitle: String,
    val reviewText: String,
    val rating: Int,
    val author: String,
    val authorName: String,
    val tags: List<String>,
    val likes: List<String>,
    val date: String
)
