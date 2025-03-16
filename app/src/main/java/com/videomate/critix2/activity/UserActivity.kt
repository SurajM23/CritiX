package com.videomate.critix2.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.squareup.picasso.Picasso
import com.videomate.critix.R
import com.videomate.critix2.adapter.UserPostsAdapter
import com.videomate.critix2.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.ActivityUserBinding
import com.videomate.critix2.model.*
import com.videomate.critix2.repository.UserRepository
import com.videomate.critix2.utils.Constants
import com.videomate.critix2.utils.SharedPrefManager
import com.videomate.critix2.viewModel.UserViewModel
import com.videomate.critix2.viewModel.UserViewModelFactory

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPostsAdapter: UserPostsAdapter
    private val userPosts = mutableListOf<UserPost>()
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFields()
        initViewModel()
        setupObservers()
        fetchUserData()
        setupRecyclerView(StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL))
        setTabSelected(binding.staggeredViewButton)
        setOnClickListeners()
    }

    private fun initFields() {
        token = SharedPrefManager.getToken(this)
    }

    private fun initViewModel() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setupObservers() {
        observeUserData()
        observeUserPosts()
    }

    private fun observeUserData() {
        userViewModel.userData.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.user?.let {
                    updateUserUI(it)
                    updateConnectionStatus(it)
                }
            }
        }
    }

    private fun observeUserPosts() {
        userViewModel.userPostsResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.data?.posts?.let {
                    updateUserPosts(it)
                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.linearViewButton.setOnClickListener {
            switchTab(
                LinearLayoutManager(this),
                binding.linearViewButton,
                binding.staggeredViewButton
            )
        }

        binding.staggeredViewButton.setOnClickListener {
            switchTab(
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL),
                binding.staggeredViewButton,
                binding.linearViewButton
            )
        }

        binding.connectButton.setOnClickListener { handleToggleConnectionClick() }
    }

    private fun setupRecyclerView(layoutManager: RecyclerView.LayoutManager) {
        userPostsAdapter = UserPostsAdapter(userPosts) { user ->
            navigateToReviewActivity(user)
        }
        binding.userPostsRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = userPostsAdapter
        }
    }

    private fun switchTab(
        layoutManager: RecyclerView.LayoutManager,
        selectedButton: View,
        unselectedButton: View
    ) {
        setupRecyclerView(layoutManager)
        setTabSelected(selectedButton)
        setTabUnselected(unselectedButton)
    }

    private fun fetchUserData() {
        token?.let {
            userViewModel.fetchUserData(Constants.USER_ID, it)
            userViewModel.fetchUserPosts(it, ReviewRequestData2(Constants.USER_ID, 1, 10))
        }
    }

    private fun updateUserUI(userDetails: UserDetails) {
        // Handle null `username` or shared preference mismatch
        if (userDetails.username.isNullOrEmpty() || userDetails.username == SharedPrefManager.getUsername(this@UserActivity)) {
            binding.connectButton.visibility = View.GONE
        } else {
            binding.connectButton.visibility = View.VISIBLE
        }

        // Safely update TextViews with null checks
        binding.usernameTextView.text = userDetails.username ?: "Unknown User"
        binding.bioTextView.text = userDetails.description ?: "Hello there"
        userDetails.myConnections.size.toString().also { binding.myConnectionsCount.text = it }
        userDetails.connectedTo.size.toString().also { binding.connectedToCount.text = it }
        userDetails.reviews.size.toString().also { binding.reviewCount.text = it }

        if (!userDetails.profileImageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(userDetails.profileImageUrl)
                .placeholder(R.drawable.ic_account)
                .error(R.drawable.ic_account)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_account)
        }
    }


    private fun updateConnectionStatus(userDetails: UserDetails) {
        val userId = SharedPrefManager.getUserId(this)
        updateConnectButton(userDetails.myConnections.contains(userId))
    }

    private fun updateUserPosts(posts: List<Post>) {
        userPosts.clear()
        userPosts.addAll(posts.map {
            UserPost(
                title = it.movieTitle,
                review = it.reviewText,
                reviewId = it._id,
                userId = it.author._id
            )
        })
        userPostsAdapter.notifyDataSetChanged()
    }

    private fun handleToggleConnectionClick() {
        val connectRequest = ConnectRequestData(Constants.USER_ID)
        token?.let { userViewModel.toggleConnection(it, connectRequest) }

        userViewModel.toggleConnectionResponse.observe(this) { response ->
            response?.let {
                showToast("Connection ${if (it.data.connected) "established" else "removed"}")
                updateConnectButton(it.data.connected)
            }
        }
    }

    private fun updateConnectButton(isConnected: Boolean) {
        binding.connectButton.text = if (isConnected) "Disconnect" else "Connect"
    }

    private fun navigateToReviewActivity(user: UserPost) {
        val intent = Intent(this, ReviewActivity::class.java).apply {
            Constants.REVIEW_ID = user.reviewId
            Constants.USER_ID = user.userId
        }
        startActivity(intent)
    }

    private fun setTabSelected(button: View) {
        button.isSelected = true
        button.setBackgroundResource(R.drawable.selected_tab_background)
        if (button is TextView) {
            button.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

    private fun setTabUnselected(button: View) {
        button.isSelected = false
        button.setBackgroundResource(R.drawable.unselected_tab_background)
        if (button is TextView) {
            button.setTextColor(ContextCompat.getColor(this, R.color.maroon))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
