package com.videomate.critix.activity

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
import com.videomate.critix.R
import com.videomate.critix.adapter.UserPostsAdapter
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.ActivityUserBinding
import com.videomate.critix.model.ConnectRequestData
import com.videomate.critix.model.Post
import com.videomate.critix.model.ReviewRequestData2
import com.videomate.critix.model.UserDetails
import com.videomate.critix.model.UserPost
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPostsAdapter: UserPostsAdapter
    private val userPosts = mutableListOf<UserPost>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        setupRecyclerView(StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL))
        setTabSelected(binding.staggeredViewButton)
        loadUserData()
        setOnClickListeners()
    }

    private fun initViewModel() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun loadUserData() {
        val userId = Constants.USER_ID
        val token = SharedPrefManager.getToken(this)

        if (!userId.isNullOrEmpty() && !token.isNullOrEmpty()) {
            fetchUserData(userId, token)
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
                StaggeredGridLayoutManager(
                    2,
                    StaggeredGridLayoutManager.VERTICAL
                ), binding.staggeredViewButton, binding.linearViewButton
            )
        }
        binding.connectButton.setOnClickListener { handleToggleConnectionClick() }
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

    private fun setupRecyclerView(layoutManager: RecyclerView.LayoutManager) {
        userPostsAdapter = UserPostsAdapter(userPosts)
        binding.userPostsRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = userPostsAdapter
        }
    }

    private fun fetchUserData(userId: String, token: String) {
        observeUserData()
        userViewModel.fetchUserData(userId, token)
        userViewModel.fetchUserPosts(token, ReviewRequestData2(userId, 1, 10))
    }

    private fun observeUserData() {
        userViewModel.userData.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userDetails = userResponse.data.user
                    updateUserUI(userDetails)
                    updateConnectionStatus(userDetails)
                }
            }
        }

        userViewModel.userPostsResponse.observe(this) { response ->
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    updateUserPosts(apiResponse.data.posts)
                }
            }
        }
    }

    private fun updateUserUI(userDetails: UserDetails) {
        binding.usernameTextView.text = userDetails.username
        binding.bioTextView.text =
            if (userDetails.myConnections.isNotEmpty()) "Hello, welcome to my profile!" else "Hello there!"
        binding.myConnectionsCount.text = userDetails.myConnections.size.toString()
        binding.connectedToCount.text = userDetails.connectedTo.size.toString()
        binding.reviewCount.text = userDetails.reviews.size.toString()
    }

    private fun updateConnectionStatus(userDetails: UserDetails) {
        val userId = SharedPrefManager.getUserId(this)
        if (userDetails.myConnections.contains(userId)) {
            updateConnectButton(true)
        }else{
            updateConnectButton(false)
        }
    }

    private fun updateUserPosts(posts: List<Post>) {
        userPosts.clear()
        userPosts.addAll(posts.map { UserPost(title = it.movieTitle, review = it.reviewText) })
        userPostsAdapter.notifyDataSetChanged()
    }

    private fun handleToggleConnectionClick() {
        val connectingId = Constants.USER_ID
        val token = SharedPrefManager.getToken(this)

        if (!connectingId.isNullOrEmpty() && !token.isNullOrEmpty()) {
            makeToggleApiCall(connectingId, token)
        }
    }

    private fun makeToggleApiCall(connectingId: String, token: String) {
        val connectRequest = ConnectRequestData(connectingId)
        userViewModel.toggleConnection(token, connectRequest)

        userViewModel.toggleConnectionResponse.observe(this) { response ->
            response?.let {
                showToast("connection ${it.data.connected}")
                updateConnectButton(it.data.connected)
            }
        }
    }

    private fun updateConnectButton(isConnected: Boolean) {
        binding.connectButton.apply {
            text = if (isConnected) "Disconnect" else "Connect"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
}
