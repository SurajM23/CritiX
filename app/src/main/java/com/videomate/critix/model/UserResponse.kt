package com.videomate.critix.model

data class UserResponse(
    val status: Int,
    val data: UserData,
    val message: String,
    val success: Boolean
)

data class UserData(
    val user: UserDetails
)

data class UserDetails(
    val _id: String,
    val username: String,
    val email: String,
    val videos: List<String>,
    val myConnections: List<String>,
    val connectedTo: List<String>,
    val date: String,
    val reviews: List<String>
)
