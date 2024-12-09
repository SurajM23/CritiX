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
import com.videomate.critix.model.User
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

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
        getUserData()
        userAdapter = UserAdapter(this)
        binding.rvUsers.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = userAdapter
        }

        observeUsers()
        observeConnectionResponse()
    }

    private fun loadToken() {
        userId = SharedPrefManager.getUserId(requireContext()).toString()
        token = SharedPrefManager.getToken(requireContext()).toString()
    }

    private fun getUserData() {
        if (userId.isNotEmpty() && token.isNotEmpty()) {
            observeUserData()
            userViewModel.fetchUserData(userId, token)
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeUserData() {
        userViewModel.userData.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    if (userResponse.data.user.connectedTo.isEmpty()) {
                        userViewModel.fetchUsers()
                        binding.rvUsers.visibility = View.VISIBLE
                        binding.rvFeed.visibility = View.GONE
                    } else {
                        binding.rvUsers.visibility = View.GONE
                        binding.rvFeed.visibility = View.VISIBLE
                        userViewModel.fetchUserFeed(token, userId, 1, 20)
                        observeFeedData()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error: Unable to load profile data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


    private fun observeFeedData() {
        userViewModel.userFeedResponse.observe(viewLifecycleOwner) { response ->
            Log.e("userFeed", "feed size: one")
            if (response != null) {
                Log.e("userFeed", "feed size: two")
                if (response.isSuccessful) {
                    Log.e("userFeed", "feed size: three")
                    response.body()?.let { userResponse ->
                        Log.e("userFeed", "feed size: four")
                        val reviews = userResponse.data.reviews
                        Log.e("userFeed", "feed size: ${reviews.size}")
                        reviewsAdapter.updateData(reviews)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error: Unable to load feed data",
                        Toast.LENGTH_SHORT
                    ).show()
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
                // Handle review click
                val intent = Intent(requireContext(), ReviewActivity::class.java)
                Constants.REVIEW_ID = reviewId
                startActivity(intent)
            },
            onAuthorClick = { authorId ->
                // Handle author click
                val intent = Intent(
                    requireContext(),
                    UserActivity::class.java
                ) // Hypothetical activity for author profiles
                Constants.USER_ID = authorId
                startActivity(intent)
            }
        )


        binding.rvFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewsAdapter
        }
    }

    private fun observeUsers() {
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
                Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeConnectionResponse() {
        userViewModel.toggleConnectionResponse.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                if (response.success) {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()

                    // Update the connection status in the adapter
                    userAdapter.updateConnectionStatus(
                        userId = Constants.USER_ID,
                        isConnected = true
                    )
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to toggle connection. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onItemClick(user: User) {
        Constants.USER_ID = user.id
        startActivity(Intent(requireContext(), UserActivity::class.java))
    }

    override fun onButtonClick(user: User) {
        SharedPrefManager.getUserId(requireContext())
            ?.let { ConnectRequestData(user.id) }?.let {
                userViewModel.toggleConnection(token, it)
            }
    }


}
