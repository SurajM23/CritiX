package com.videomate.critix.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.videomate.critix.R
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.ActivityReviewBinding
import com.videomate.critix.model.LikeReviewRequest
import com.videomate.critix.model.ReviewDetails
import com.videomate.critix.repository.UserRepository
import com.videomate.critix.utils.Constants
import com.videomate.critix.utils.SharedPrefManager
import com.videomate.critix.viewModel.UserViewModel
import com.videomate.critix.viewModel.UserViewModelFactory

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var reviewId: String
    private lateinit var userId: String
    private var isLiked = false
    private var totalLikes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reviewId = Constants.REVIEW_ID
        userId = SharedPrefManager.getUserId(this@ReviewActivity).toString()
        initView()
        observeViewModel()
        if (reviewId.isNotEmpty()) {
            viewModel.fetchReviewById(reviewId, userId)
        } else {
            Toast.makeText(this, "Review ID is missing", Toast.LENGTH_SHORT).show()
        }
        binding.likeSection.setOnClickListener {
            handleLikeAction()
        }

    }

    private fun initView() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.singleReviewResponse.observe(this) { response ->
            if (response != null) {
                if (response.isSuccessful) {
                    val review = response.body()?.data
                    Log.e("reviewNameUser", "name ${Constants.REVIEW_ID}")
                    Log.e("reviewNameUser", "name ${review?.author}")
                    if (review != null) {
                        displayReviewDetails(review)
                    } else {
                        Toast.makeText(this, "No review details found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.likeReviewResponse.observe(this) { response ->
            if (response != null && response.isSuccessful) {
                val data = response.body()?.data
                if (data != null) {
                    isLiked = data.liked
                    totalLikes = data.totalLikes

                    // Update the UI
                    binding.tvLikeCount.text = "$totalLikes"
                    binding.ivLikeIcon.setImageResource(
                        if (isLiked) R.drawable.ic_like_on else R.drawable.ic_like_off
                    )
                }
            } else {
                Toast.makeText(this, "Failed to like the review", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayReviewDetails(review: ReviewDetails) {
        with(binding) {
            tvMovieTitle.text = review.movieTitle
            tvReviewText.text = review.reviewText
            val rating = review.rating.coerceIn(0, 5) // Ensure the rating is between 0 and 5
            val ratingStars = "★".repeat(rating) + "☆".repeat(5 - rating)
            tvRating.text = ratingStars
            review.likes.size.toString().also { tvLikeCount.text = it }
            tvName.text = review.author.username
            tvTags.text = review.tags.joinToString(", ") { "#$it" }
            isLiked = review.isLiked
            totalLikes = review.likes.size
            ivLikeIcon.setImageResource(
                if (isLiked) R.drawable.ic_like_on else R.drawable.ic_like_off
            )
            if (review.author.profileImageUrl?.isNotEmpty() == true) {
                Picasso.get()
                    .load(review.author.profileImageUrl)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .into(binding.profileImage)
            } else binding.profileImage.setImageResource(R.drawable.ic_account)


            binding.llUser.setOnClickListener {
                val intent = Intent(
                    this@ReviewActivity,
                    UserActivity::class.java
                )
                startActivity(intent)
            }
        }
    }

    private fun handleLikeAction() {
        val likeRequest = LikeReviewRequest(reviewId, userId)
        viewModel.likeReview(likeRequest)
    }
}
