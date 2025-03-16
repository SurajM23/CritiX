package com.videomate.critix2.repository

import com.videomate.critix2.apiService.ApiService
import com.videomate.critix2.model.ApiResponse
import com.videomate.critix2.model.ConnectRequestData
import com.videomate.critix2.model.ConnectionResponse
import com.videomate.critix2.model.LikeReviewRequest
import com.videomate.critix2.model.LikeReviewResponse
import com.videomate.critix2.model.LoginRequest
import com.videomate.critix2.model.LoginResponse
import com.videomate.critix2.model.RegisterRequest
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

class UserRepository(private val apiService: ApiService) {

    suspend fun registerUser(request: RegisterRequest) = apiService.registerUser(request)

    suspend fun loginUser(request: LoginRequest): Response<LoginResponse> {
        return apiService.loginUser(request)
    }

    suspend fun getUserData(userId: String, token: String): Response<UserResponse> {
        return apiService.getUserData(userId, "Bearer $token")
    }

    suspend fun createReview(
        token: String,
        reviewRequest: ReviewRequest
    ): Response<ReviewResponse> {
        return apiService.createReview("Bearer $token", reviewRequest)
    }

    suspend fun getReviews(request: ReviewRequestData): Response<ReviewResponse2> {
        return apiService.getReviews(request)
    }


    suspend fun getUserPosts(token: String, request: ReviewRequestData2): Response<ApiResponse> {
        return apiService.getUserPosts("Bearer $token", request)
    }

    suspend fun getAllUsers(token: String): Response<UserResponse2> {
        return apiService.getAllUsers("Bearer $token")
    }


    suspend fun toggleConnection(
        token: String,
        request: ConnectRequestData
    ): Response<ConnectionResponse> {
        return apiService.toggleConnection("Bearer $token", request)
    }

    suspend fun getUserFeed(
        token: String,
        userId: String,
        page: Int,
        limit: Int
    ): Response<ReviewResponse2> {
        return apiService.getUserFeed("Bearer $token", userId, page, limit)
    }

    suspend fun getReviewById(reviewId: String, userId: String): Response<SingleReviewResponse> {
        return apiService.getReviewById(reviewId, userId)
    }

    suspend fun likeReview(likeReviewRequest: LikeReviewRequest): Response<LikeReviewResponse> {
        return apiService.likeReview(likeReviewRequest)
    }

    suspend fun updateUserDetails(
        token: String,
        updateUserRequest: UpdateUserRequest
    ): Response<UpdateUserResponse> {
        return apiService.updateUserDetails("Bearer $token", updateUserRequest)
    }


    suspend fun updateProfileImage(
        token: String,
        imageFile: File
    ): Response<UpdateProfileImageResponse> {
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val multipartBody =
            MultipartBody.Part.createFormData("profileImage", imageFile.name, requestBody)
        return apiService.updateProfileImage("Bearer $token", multipartBody)
    }

    suspend fun updateReview(
        token: String,
        reviewId: String,
        reviewRequest: ReviewRequest
    ): Response<ReviewResponse> {
        return apiService.updateReview("Bearer $token", reviewId, reviewRequest)
    }

    suspend fun deleteReview(token: String, reviewId: String): Response<ApiResponse> {
        return apiService.deleteReview("Bearer $token", reviewId)
    }

}