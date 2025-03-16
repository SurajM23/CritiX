package com.videomate.critix2.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.videomate.critix2.activity.ReviewActivity
import com.videomate.critix2.adapter.ReviewAdapter2
import com.videomate.critix2.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.FragmentExploreBinding
import com.videomate.critix2.model.Review2
import com.videomate.critix2.model.ReviewRequestData
import com.videomate.critix2.repository.UserRepository
import com.videomate.critix2.utils.Constants
import com.videomate.critix2.viewModel.UserViewModel
import com.videomate.critix2.viewModel.UserViewModelFactory

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserViewModel
    private lateinit var adapter: ReviewAdapter2
    private var reviewsList = mutableListOf<Review2>()
    private var currentPage = 1
    private val pageLimit = 10
    private var isLoading = false
    private var isLastPage = false

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
        setupRecyclerView()
        fetchReviews()
    }

    private fun fetchReviews() {
        if (isLoading || isLastPage) return
        isLoading = true

        val requestData = ReviewRequestData(page = currentPage, limit = pageLimit)
        viewModel.fetchReviews(requestData)
        viewModel.reviewsResponse.observe(viewLifecycleOwner) { response ->
            isLoading = false
            if (response.isSuccessful) {
                val reviews = response.body()?.data?.reviews ?: emptyList()
                val uniqueReviews = reviews.filter { newReview ->
                    reviewsList.none { existingReview -> existingReview._id == newReview._id }
                }
                if (uniqueReviews.isEmpty()) {
                    isLastPage = true
                } else {
                    reviewsList.addAll(uniqueReviews)
                    adapter.notifyItemRangeInserted(
                        (reviewsList.size - uniqueReviews.size),
                        uniqueReviews.size
                    )
                    currentPage++
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load reviews", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun setupRecyclerView() {
        adapter = ReviewAdapter2(reviewsList) { reviewId ->
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

        // Add scroll listener for pagination
        binding.recyclerViewReviews.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPositions =
                        layoutManager.findFirstVisibleItemPositions(null)
                    val firstVisibleItemPosition = firstVisibleItemPositions.minOrNull() ?: 0

                    if (!isLoading && !isLastPage) {
                        if (visibleItemCount + firstVisibleItemPosition >= totalItemCount) {
                            fetchReviews()
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (Constants.ReviewUploaded1) {
            Constants.ReviewUploaded1 = false
            currentPage = 1
            isLastPage = false
            reviewsList.clear()
            adapter.notifyDataSetChanged()
            fetchReviews()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
