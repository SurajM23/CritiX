package com.videomate.critix.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videomate.critix.model.*
import com.videomate.critix.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _registerResponse = MutableLiveData<Response<RegisterResponse>>()
    val registerResponse: LiveData<Response<RegisterResponse>> get() = _registerResponse

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            try {
                val response = repository.registerUser(request)
                _registerResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("RegisterUser", "Error: ${e.message}")
                e.printStackTrace()
            }
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
                Log.e("LoginUser", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private val _userData = MutableLiveData<Response<UserResponse>>()
    val userData: LiveData<Response<UserResponse>> get() = _userData
    fun fetchUserData(userId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserData(userId, token)
                _userData.postValue(response)
            } catch (e: Exception) {
                Log.e("FetchUserData", "Error: ${e.message}")
                e.printStackTrace()
            }
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
                Log.e("CreateReview", "Error: ${e.message}")
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
                Log.e("FetchReviews", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private val _userPostsResponse = MutableLiveData<Response<ApiResponse>>()
    val userPostsResponse: LiveData<Response<ApiResponse>> get() = _userPostsResponse
    fun fetchUserPosts(token: String, request: ReviewRequestData2) {
        viewModelScope.launch {
            try {
                val response = repository.getUserPosts(token, request)
                _userPostsResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("FetchUserPosts", "Error: ${e.message}")
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
                Log.e("FetchUsers", "Error: ${e.message}")
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
                    _toggleConnectionResponse.postValue(null)
                }
            } catch (e: Exception) {
                Log.e("ToggleConnection", "Error: ${e.message}")
                e.printStackTrace()
                _toggleConnectionResponse.postValue(null)
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
                    _userFeedResponse.postValue(null)
                }
            } catch (e: Exception) {
                Log.e("FetchUserFeed", "Error: ${e.message}")
                e.printStackTrace()
                _userFeedResponse.postValue(null)
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
                Log.e("FetchReviewById", "Error: ${e.message}")
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
                Log.e("LikeReview", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private val _updateUserResponse = MutableLiveData<Response<UpdateUserResponse>>()
    val updateUserResponse: LiveData<Response<UpdateUserResponse>> get() = _updateUserResponse

    fun updateUserDetails(token: String, updateUserRequest: UpdateUserRequest) {
        viewModelScope.launch {
            try {
                Log.d("UpdateUserDetails", "Making API call to update user details...")

                val response = repository.updateUserDetails(token, updateUserRequest)

                if (response.isSuccessful) {
                    Log.d("UpdateUserDetails", "Response successful: ${response.raw()}")
                    _updateUserResponse.postValue(response)
                } else {
                    Log.e("UpdateUserDetails", "API call failed. Response: ${response.raw()}")
                }

            } catch (e: Exception) {
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
                    _updateProfileImageResponse.postValue(null)
                }
            } catch (e: Exception) {
                Log.e("UpdateProfileImage", "Error: ${e.message}")
                e.printStackTrace()
                _updateProfileImageResponse.postValue(null)
            }
        }
    }

    private val _updateReviewResponse = MutableLiveData<Response<ReviewResponse>>()
    fun updateReview(token: String, reviewId: String, reviewRequest: ReviewRequest) {
        viewModelScope.launch {
            try {
                val response = repository.updateReview(token, reviewId, reviewRequest)
                Log.e("updateReviewApi","review ${response.raw()}")
                _updateReviewResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("UpdateReview", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private val _deleteReviewResponse = MutableLiveData<Response<ApiResponse>>()
    val deleteReviewResponse: LiveData<Response<ApiResponse>> get() = _deleteReviewResponse

    fun deleteReview(token: String, reviewId: String) {
        viewModelScope.launch {
            try {
                val response = repository.deleteReview(token, reviewId)
                _deleteReviewResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("DeleteReview", "Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
