package com.videomate.critix2.model

data class ReviewRequest(
    val movieTitle: String,
    val reviewText: String,
    val rating: Int,
    val tags: List<String>
)
