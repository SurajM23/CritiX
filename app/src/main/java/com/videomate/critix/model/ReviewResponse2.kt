package com.videomate.critix.model

data class ReviewResponse2(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: ReviewData2
)

data class ReviewData2(
    val reviews: List<Review2>,
    val totalReviews: Int,
    val totalPages: Int,
    val currentPage: Int,
    val reviewsPerPage: Int
)

data class Review2(
    val _id: String,
    val movieTitle: String,
    val reviewText: String,
    val rating: Int,
    val author: Author,
    val tags: List<String>,
    val likes: List<String>,
    val date: String
)

data class Author(
    val _id: String,
    val username: String
)
