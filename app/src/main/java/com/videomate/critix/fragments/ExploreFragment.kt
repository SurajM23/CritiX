package com.videomate.critix.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.videomate.critix.activity.ReviewActivity
import com.videomate.critix.adapter.ReviewAdapter2
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.FragmentExploreBinding
import com.videomate.critix.model.Review2
import com.videomate.critix.model.ReviewDetails
import com.videomate.critix.model.ReviewRequestData
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val apiService = ApiServiceBuilder.createApiService()
        val repository = UserRepository(apiService)
        viewModel =
            ViewModelProvider(this, UserViewModelFactory(repository))[UserViewModel::class.java]
        fetchReviews()
    }

    private fun fetchReviews() {
        val requestData = ReviewRequestData(page = 1, limit = 20)
        viewModel.fetchReviews(requestData)
        viewModel.reviewsResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                val reviews = response.body()?.data?.reviews ?: emptyList()
                setupRecyclerView(reviews)
            } else {
                Toast.makeText(requireContext(), "Failed to load reviews", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupRecyclerView(reviews: List<Review2>) {
        val adapter = ReviewAdapter2(reviews) { reviewId ->
            val intent = Intent(requireContext(), ReviewActivity::class.java)
            Constants.REVIEW_ID = reviewId
            startActivity(intent)
        }

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(
            2,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding.recyclerViewReviews.layoutManager = staggeredGridLayoutManager
        binding.recyclerViewReviews.adapter = adapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
