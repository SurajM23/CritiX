package com.videomate.critix.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.videomate.critix.model.UpdateProfileImageResponse
import com.videomate.critix.model.UpdateProfileResponse
import com.videomate.critix.model.UpdateUserRequest
import com.videomate.critix.model.UpdateUserResponse
import com.videomate.critix.model.UserResponse
import com.videomate.critix.model.UserResponse2
import com.videomate.critix.repository.UserRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response
import java.io.File

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _registerResponse = MutableLiveData<Response<RegisterResponse>>()
    val registerResponse: LiveData<Response<RegisterResponse>> get() = _registerResponse

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            val response = repository.registerUser(request)
            _registerResponse.postValue(response)
        }
    }


    private val _loginResponse = MutableLiveData<Response<LoginResponse>>()
    val loginResponse: LiveData<Response<LoginResponse>> get() = _loginResponse
    fun loginUser(request: LoginRequest) {
        viewModelScope.launch {
            try {
                val response = repository.loginUser(request)
                _loginResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val _userData = MutableLiveData<Response<UserResponse>>()
    val userData: LiveData<Response<UserResponse>> get() = _userData
    fun fetchUserData(userId: String, token: String) {
        viewModelScope.launch {
            val response = repository.getUserData(userId, token)
            _userData.postValue(response)
        }
    }


    private val _reviewResponse = MutableLiveData<Response<ReviewResponse>>()
    val reviewResponse: LiveData<Response<ReviewResponse>> get() = _reviewResponse
    fun createReview(token: String, reviewRequest: ReviewRequest) {
        viewModelScope.launch {
            try {
                val response = repository.createReview(token, reviewRequest)
                _reviewResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val _reviewsResponse = MutableLiveData<Response<ReviewResponse2>>()
    val reviewsResponse: LiveData<Response<ReviewResponse2>> get() = _reviewsResponse
    fun fetchReviews(request: ReviewRequestData) {
        viewModelScope.launch {
            try {
                val response = repository.getReviews(request)
                _reviewsResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val _userPostsResponse = MutableLiveData<Response<ApiResponse>>()
    val userPostsResponse: LiveData<Response<ApiResponse>> get() = _userPostsResponse
    fun fetchUserPosts(token: String,request: ReviewRequestData2) {
        viewModelScope.launch {
            try {
                val response = repository.getUserPosts(token,request)
                _userPostsResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private val _usersResponse = MutableLiveData<Response<UserResponse2>>()
    val usersResponse: LiveData<Response<UserResponse2>> get() = _usersResponse
    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = repository.getAllUsers()
                _usersResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    private val _toggleConnectionResponse = MutableLiveData<ConnectionResponse?>()
    val toggleConnectionResponse: LiveData<ConnectionResponse?> get() = _toggleConnectionResponse
    fun toggleConnection(token: String, request: ConnectRequestData) {
        viewModelScope.launch {
            try {
                val response = repository.toggleConnection(token, request)
                if (response.isSuccessful) {
                    _toggleConnectionResponse.postValue(response.body())
                } else {
                    _toggleConnectionResponse.postValue(null) // Handle API error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _toggleConnectionResponse.postValue(null) // Handle failure
            }
        }
    }

    private val _userFeedResponse = MutableLiveData<Response<ReviewResponse2>?>()
    val userFeedResponse: LiveData<Response<ReviewResponse2>?> get() = _userFeedResponse
    fun fetchUserFeed(token: String, userId: String, page: Int, limit: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getUserFeed(token, userId, page, limit)
                if (response.isSuccessful) {
                    _userFeedResponse.postValue(response)
                } else {
                    _userFeedResponse.postValue(null) // Handle errors
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userFeedResponse.postValue(null) // Handle failures
            }
        }
    }


    private val _singleReviewResponse = MutableLiveData<Response<SingleReviewResponse>>()
    val singleReviewResponse: LiveData<Response<SingleReviewResponse>> get() = _singleReviewResponse

    fun fetchReviewById(reviewId: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = repository.getReviewById(reviewId, userId)
                _singleReviewResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _likeReviewResponse = MutableLiveData<Response<LikeReviewResponse>>()
    val likeReviewResponse: LiveData<Response<LikeReviewResponse>> get() = _likeReviewResponse

    fun likeReview(likeReviewRequest: LikeReviewRequest) {
        viewModelScope.launch {
            try {
                val response = repository.likeReview(likeReviewRequest)
                _likeReviewResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




    private val _updateUserResponse = MutableLiveData<Response<UpdateUserResponse>>()
    val updateUserResponse: LiveData<Response<UpdateUserResponse>> get() = _updateUserResponse

    fun updateUserDetails(token: String, updateUserRequest: UpdateUserRequest) {
        viewModelScope.launch {
            try {
                // Log before making the API call
                Log.d("UpdateUserDetails", "Making API call to update user details...")

                val response = repository.updateUserDetails(token, updateUserRequest)

                // Log response status and body
                if (response.isSuccessful) {
                    Log.d("UpdateUserDetails", "Response successful: ${response.raw()}")
                    _updateUserResponse.postValue(response)
                } else {
                    Log.e("UpdateUserDetails", "API call failed. Response: ${response.raw()}")
                }

            } catch (e: Exception) {
                // Log any exceptions that occur during the API call
                Log.e("UpdateUserDetails", "Error during API call: ${e.message}")
                e.printStackTrace()
            }
        }
    }



    private val _updateProfileImageResponse = MutableLiveData<UpdateProfileImageResponse?>()
    val updateProfileImageResponse: LiveData<UpdateProfileImageResponse?> get() = _updateProfileImageResponse

    fun updateProfileImage(token: String, imageFile: File) {
        viewModelScope.launch {
            try {
                val response = repository.updateProfileImage(token, imageFile)
                if (response.isSuccessful) {
                    _updateProfileImageResponse.postValue(response.body())
                } else {
                    _updateProfileImageResponse.postValue(null) // Handle API error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _updateProfileImageResponse.postValue(null) // Handle failure
            }
        }
    }




}
