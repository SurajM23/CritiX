package com.videomate.critix.model

data class ApiResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: Data
)

data class Data(
    val posts: List<Post>,
    val totalPosts: Int,
    val totalPages: Int,
    val currentPage: Int,
    val postsPerPage: Int
)

data class Post(
    val _id: String,
    val movieTitle: String,
    val reviewText: String,
    val rating: Int,
    val author: Author,
    val authorName: String?,
    val tags: List<String>,
    val likes: List<Any>,
    val createdAt: String?,
    val date: String,
    val __v: Int
)

