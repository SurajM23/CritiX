package com.videomate.critix2.model

data class ConnectionResponse(
    val status: Int,
    val message: String,
    val data: ConnectionData,
    val success: Boolean
)
data class ConnectionData(
    val connected: Boolean,
    val connectingId: String
)
