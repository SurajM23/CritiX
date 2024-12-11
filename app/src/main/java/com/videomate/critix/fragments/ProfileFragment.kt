package com.videomate.critix.fragments

import android.content.Intent
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
import com.squareup.picasso.Picasso
import com.videomate.critix.R
import com.videomate.critix.activity.EditProfileActivity
import com.videomate.critix.activity.ReviewActivity
import com.videomate.critix.adapter.UserPostsAdapter
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.FragmentProfileBinding
import com.videomate.critix.model.ReviewRequestData2
import com.videomate.critix.model.UserPost
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
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
        setupRecyclerView(StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL))
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
        binding.txtEdit.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
    }

    private fun fetchUserData(userId: String, token: String) {
        observeUserData()
        userViewModel.fetchUserData(userId, token)
        userViewModel.fetchUserPosts(token, ReviewRequestData2(userId, 1, 20))
    }


    private fun observeUserData() {
        userViewModel.userData.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.let { userResponse ->
                    val userDetails = userResponse.data.user

                    // Use safe call to ensure userDetails is not null
                    userDetails?.let { details ->
                        binding.usernameTextView.text = details.username ?: "Username not available"
                        binding.bioTextView.text = details.description ?: "No description available"
                        binding.myConnectionsCount.text =
                            userResponse.data.user.myConnections?.size?.toString() ?: "0"
                        binding.connectedToCount.text =
                            userResponse.data.user.connectedTo?.size?.toString() ?: "0"
                        binding.reviewCount.text = userResponse.data.user.reviews?.size?.toString() ?: "0"

                        // Safe check for profileImageUrl being null
                        if (!details.profileImageUrl.isNullOrEmpty()) {
                            Picasso.get()
                                .load(details.profileImageUrl)
                                .placeholder(R.drawable.ic_account)
                                .error(R.drawable.ic_account)
                                .into(binding.profileImage)
                        } else {
                            binding.profileImage.setImageResource(R.drawable.ic_account)
                        }
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

        userViewModel.userPostsResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    val posts = apiResponse.data.posts?.map { post ->
                        UserPost(
                            title = post.movieTitle ?: "Untitled",
                            review = post.reviewText ?: "No review available",
                            reviewId = post._id,
                            userId = post.author._id
                        )
                    }
                    if (posts != null) {
                        updateUserPosts(posts)
                    }
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
        { user ->
            val intent = Intent(requireContext(), ReviewActivity::class.java)
            Constants.REVIEW_ID = user.reviewId
            Constants.USER_ID = user.userId
            startActivity(intent)
        }
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

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
