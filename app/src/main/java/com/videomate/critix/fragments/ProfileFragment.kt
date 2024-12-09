package com.videomate.critix.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.videomate.critix.R
import com.videomate.critix.adapter.UserPostsAdapter
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.FragmentProfileBinding
import com.videomate.critix.model.ReviewRequestData2
import com.videomate.critix.model.UserPost
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPostsAdapter: UserPostsAdapter
    private val userPosts = mutableListOf<UserPost>() // List to hold posts

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setupRecyclerView(  StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL))
        setTabSelected(binding.staggeredViewButton)
        setOnClick()
        loadUserData()
    }

    private fun loadUserData() {
        val userId = SharedPrefManager.getUserId(requireContext())
        val token = SharedPrefManager.getToken(requireContext())

        if (!userId.isNullOrEmpty() && !token.isNullOrEmpty()) {
            fetchUserData(userId, token)
        } else {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun init() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setOnClick() {
        binding.linearViewButton.setOnClickListener {
            setupRecyclerView(LinearLayoutManager(requireContext()))
            setTabSelected(binding.linearViewButton)
            setTabUnselected(binding.staggeredViewButton)
        }
        binding.staggeredViewButton.setOnClickListener {
            setupRecyclerView(
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            )
            setTabSelected(binding.staggeredViewButton)
            setTabUnselected(binding.linearViewButton)
        }
    }

    private fun fetchUserData(userId: String, token: String) {
        observeUserData()
        userViewModel.fetchUserData(userId, token)
        userViewModel.fetchUserPosts(token, ReviewRequestData2(userId, 1, 10))
    }

    private fun observeUserData() {
        userViewModel.userData.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userDetails = userResponse.data.user
                    binding.usernameTextView.text = userDetails.username
                    binding.bioTextView.text = if (userDetails.myConnections.isNotEmpty()) {
                        "Hello, welcome to my profile!"
                    } else {
                        "Hello there!"
                    }
                    binding.myConnectionsCount.text =
                        userResponse.data.user.myConnections.size.toString()
                    binding.connectedToCount.text =
                        userResponse.data.user.connectedTo.size.toString()
                    binding.reviewCount.text = userResponse.data.user.reviews.size.toString()

                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error: Unable to load profile data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        userViewModel.userPostsResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val posts = apiResponse.data.posts.map { post ->
                        UserPost(title = post.movieTitle, review = post.reviewText)
                    }
                    updateUserPosts(posts)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Error: Unable to load posts",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateUserPosts(posts: List<UserPost>) {
        userPosts.clear()
        userPosts.addAll(posts)
        userPostsAdapter.notifyDataSetChanged() // Notify adapter of data changes
    }

    private fun setupRecyclerView(layoutManager: RecyclerView.LayoutManager) {
        userPostsAdapter = UserPostsAdapter(userPosts)
        binding.userPostsRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = userPostsAdapter
        }
    }

    private fun setTabSelected(button: View) {
        button.isSelected = true
        button.setBackgroundResource(R.drawable.selected_tab_background)
        if (button is TextView) {
            button.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }

    private fun setTabUnselected(button: View) {
        button.isSelected = false
        button.setBackgroundResource(R.drawable.unselected_tab_background)
        if (button is TextView) {
            button.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.maroon)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
