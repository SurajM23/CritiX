package com.videomate.critix2.apiService

import com.videomate.critix2.model.ApiResponse
import com.videomate.critix2.model.ConnectRequestData
import com.videomate.critix2.model.ConnectionResponse
import com.videomate.critix2.model.LikeReviewRequest
import com.videomate.critix2.model.LikeReviewResponse
import com.videomate.critix2.model.LoginRequest
import com.videomate.critix2.model.LoginResponse
import com.videomate.critix2.model.RegisterRequest
import com.videomate.critix2.model.RegisterResponse
import com.videomate.critix2.model.ReviewRequest
import com.videomate.critix2.model.ReviewRequestData
import com.videomate.critix2.model.ReviewRequestData2
import com.videomate.critix2.model.ReviewResponse
import com.videomate.critix2.model.ReviewResponse2
import com.videomate.critix2.model.SingleReviewResponse
import com.videomate.critix2.model.UpdateProfileImageResponse
import com.videomate.critix2.model.UpdateUserRequest
import com.videomate.critix2.model.UpdateUserResponse
import com.videomate.critix2.model.UserResponse
import com.videomate.critix2.model.UserResponse2
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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
    suspend fun getAllUsers( @Header("Authorization") token: String): Response<UserResponse2>

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

    @PUT("users/updateuserdata")
    suspend fun updateUserDetails(
        @Header("Authorization") token: String,
        @Body updateUserRequest: UpdateUserRequest
    ): Response<UpdateUserResponse>

    @Multipart
    @PUT("users/updateprofileimage")
    suspend fun updateProfileImage(
        @Header("Authorization") token: String,          // Token for authentication
        @Part profileImage: MultipartBody.Part          // Profile image file as part
    ): Response<UpdateProfileImageResponse>

    @PUT("review/update/{id}")
    suspend fun updateReview(
        @Header("Authorization") token: String,
        @Path("id") reviewId: String,
        @Body reviewRequest: ReviewRequest
    ): Response<ReviewResponse>

    @DELETE("review/delete/{id}")
    suspend fun deleteReview(
        @Header("Authorization") token: String,
        @Path("id") reviewId: String
    ): Response<ApiResponse>


}
