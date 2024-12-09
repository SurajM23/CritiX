package com.videomate.critix.apiService

import com.videomate.critix.model.ApiResponse
import com.videomate.critix.model.ConnectRequestData
import com.videomate.critix.model.ConnectionResponse
import com.videomate.critix.model.LikeReviewRequest
import com.videomate.critix.model.LikeReviewResponse
import com.videomate.critix.model.LoginRequest
import com.videomate.critix.model.LoginResponse
import com.videomate.critix.model.RegisterRequest
import com.videomate.critix.model.RegisterResponse
import com.videomate.critix.model.ReviewRequest
import com.videomate.critix.model.ReviewRequestData
import com.videomate.critix.model.ReviewRequestData2
import com.videomate.critix.model.ReviewResponse
import com.videomate.critix.model.ReviewResponse2
import com.videomate.critix.model.SingleReviewResponse
import com.videomate.critix.model.UserResponse
import com.videomate.critix.model.UserResponse2
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @POST("users/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("users/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @GET("users/{userId}")
    suspend fun getUserData(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @POST("review/add_review")
    suspend fun createReview(
        @Header("Authorization") token: String,
        @Body reviewRequest: ReviewRequest
    ): Response<ReviewResponse>

    @POST("review/get_reviews")
    suspend fun getReviews(@Body request: ReviewRequestData): Response<ReviewResponse2>

    @POST("review/get_user_posts")
    suspend fun getUserPosts(
        @Header("Authorization") token: String, // Include token in header
        @Body request: ReviewRequestData2       // Request body
    ): Response<ApiResponse>

    @GET("users/user_list")
    suspend fun getAllUsers(): Response<UserResponse2>

    @POST("users/connect")
    suspend fun connectUser(
        @Header("Authorization") token: String, // Include token in header
        @Body request: ConnectRequestData      // Request body
    ): Response<ConnectionResponse>

    @POST("users/toggleConnection")
    suspend fun toggleConnection(
        @Header("Authorization") token: String, // Include token in header
        @Body request: ConnectRequestData      // Request body
    ): Response<ConnectionResponse>

    @GET("users/{userId}/feed")
    suspend fun getUserFeed(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ReviewResponse2>

    @GET("review/reviews/{id}")
    suspend fun getReviewById(
        @Path("id") reviewId: String,
        @Query("userId") userId: String // Added userId as a query parameter
    ): Response<SingleReviewResponse>


    @POST("review/like")
    suspend fun likeReview(@Body likeReviewRequest: LikeReviewRequest): Response<LikeReviewResponse>

}
