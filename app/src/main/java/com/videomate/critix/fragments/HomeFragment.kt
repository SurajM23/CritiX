package com.videomate.critix.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.videomate.critix.activity.ReviewActivity
import com.videomate.critix.activity.UserActivity
import com.videomate.critix.adapter.ReviewsAdapter
import com.videomate.critix.adapter.UserAdapter
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.FragmentHomeBinding
import com.videomate.critix.model.ConnectRequestData
import com.videomate.critix.model.Review2
import com.videomate.critix.model.User
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

class HomeFragment : Fragment(), UserAdapter.OnItemClickListener {

    private lateinit var userAdapter: UserAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var userViewModel: UserViewModel
    private var token = ""
    private var userId = ""
    private lateinit var reviewsAdapter: ReviewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadToken()
        setupViewModel()
        setupRecyclerViews()
        observeUsers()
        observeConnectionResponse()
    }

    private fun loadToken() {
        userId = SharedPrefManager.getUserId(requireContext()).toString()
        token = SharedPrefManager.getToken(requireContext()).toString()
    }

    private fun getUserData() {
        safeApiCall {
            userViewModel.fetchUserData(userId, token)
        }
    }

    private fun observeUserData() {
        userViewModel.userData.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    if (userResponse.data.user.connectedTo.isEmpty()) {
                        safeApiCall {
                            userViewModel.fetchUsers()
                        }
                        binding.rvUsers.visibility = View.VISIBLE
                        binding.rvFeed.visibility = View.GONE
                    } else {
                        binding.rvUsers.visibility = View.GONE
                        binding.rvFeed.visibility = View.VISIBLE
                        safeApiCall {
                            userViewModel.fetchUserFeed(token, userId, 1, 20)
                        }
                        observeFeedData()
                    }
                }
            } else {
                showError("Unable to load profile data")
            }
        }
    }

    private fun observeFeedData() {
        userViewModel.userFeedResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    response.body()?.let { userResponse ->
                        val reviews = userResponse.data.reviews
                        reviewsAdapter.updateData(reviews)
                    }
                } else {
                    showError("Unable to load feed data")
                }
            }
        }
    }

    private fun setupViewModel() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]

        reviewsAdapter = ReviewsAdapter(
            onReviewClick = { reviewId ->
                val intent = Intent(requireContext(), ReviewActivity::class.java)
                Constants.REVIEW_ID = reviewId
                startActivity(intent)
            },
            onAuthorClick = { authorId ->
                val intent = Intent(
                    requireContext(),
                    UserActivity::class.java
                )
                Constants.USER_ID = authorId
                startActivity(intent)
            },
            onShareClick = {
                review -> onShareClicked(review)
            }
        )

        binding.rvFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewsAdapter
        }
    }

    private fun onShareClicked(review: Review2) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            val shareMessage = """
            Check out this review on Critix:
            Movie Title: ${review.movieTitle}
            Rating: ${review.rating}/5
            Tags: ${review.tags.joinToString(", ") { "#$it" }}
            
            Review: ${review.reviewText}
        """.trimIndent()
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Review via"))
    }

    private fun setupRecyclerViews() {
        userAdapter = UserAdapter(this)
        binding.rvUsers.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = userAdapter
        }
    }

    private fun observeUsers() {
        observeUserData()
        userViewModel.usersResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.let { userList ->
                    val users = userList.map {
                        User(
                            id = it.id,
                            username = it.username,
                            profileImageUrl = it.profileImageUrl
                        )
                    }
                    userAdapter.setUsers(users)
                }
            } else {
                showError("Failed to load users")
            }
        }
    }

    private fun observeConnectionResponse() {
        userViewModel.toggleConnectionResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    userAdapter.updateConnectionStatus(
                        userId = Constants.USER_ID,
                        isConnected = true
                    )
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                showError("Failed to toggle connection. Try again.")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun safeApiCall(apiCall: suspend () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    apiCall()
                }
            } catch (e: SocketTimeoutException) {
                showError("The server is taking too long to respond. Please try again later.")
            } catch (e: Exception) {
                showError("An unexpected error occurred. Please try again later.")
                Log.e("API_ERROR", "Error: ${e.message}")
            }
        }
    }

    override fun onItemClick(user: User) {
        Constants.USER_ID = user.id
        startActivity(Intent(requireContext(), UserActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        getUserData()
    }

    override fun onButtonClick(user: User) {
        SharedPrefManager.getUserId(requireContext())
            ?.let { ConnectRequestData(user.id) }?.let {
                safeApiCall {
                    userViewModel.toggleConnection(token, it)
                }
            }
    }
}