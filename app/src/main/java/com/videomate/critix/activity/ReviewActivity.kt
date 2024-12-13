package com.videomate.critix.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import com.videomate.critix.R
import com.videomate.critix.apiService.ApiServiceBuilder
import com.videomate.critix.databinding.ActivityReviewBinding
import com.videomate.critix.model.LikeReviewRequest
import com.videomate.critix.model.ReviewDetails
import com.videomate.critix.model.ReviewRequest
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
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initFields()
        initViewModel()
        setupObservers()
        fetchReviewDetails()
        setupClickListeners()
    }

    private fun initFields() {
        reviewId = Constants.REVIEW_ID
        userId = SharedPrefManager.getUserId(this).toString()
        token = SharedPrefManager.getToken(this)
    }

    private fun initViewModel() {
        val apiService = ApiServiceBuilder.createApiService()
        val userRepository = UserRepository(apiService)
        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[UserViewModel::class.java]
    }

    private fun setupObservers() {
        observeReviewDetails()
        observeLikeResponse()
        observeDeleteResponse()
    }

    private fun fetchReviewDetails() {
        if (reviewId.isNotEmpty()) {
            viewModel.fetchReviewById(reviewId, userId)
        } else {
            Toast.makeText(this, "Review ID is missing", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.likeSection.setOnClickListener { handleLikeAction() }
        binding.txtEdit.setOnClickListener { showEditDialog() }
        binding.imgDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }

    private fun observeReviewDetails() {
        viewModel.singleReviewResponse.observe(this) { response ->
            if (response?.isSuccessful == true) {
                response.body()?.data?.let { displayReviewDetails(it) }
            } else {
                Toast.makeText(this, "Error: ${response?.message()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeLikeResponse() {
        viewModel.likeReviewResponse.observe(this) { response ->
            if (response?.isSuccessful == true) {
                response.body()?.data?.let {
                    isLiked = it.liked
                    totalLikes = it.totalLikes
                    totalLikes.toString().also { binding.tvLikeCount.text = it }
                    binding.ivLikeIcon.setImageResource(if (isLiked) R.drawable.ic_like_on else R.drawable.ic_like_off)
                }
            } else {
                Toast.makeText(this, "Failed to like the review", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeDeleteResponse() {
        viewModel.deleteReviewResponse.observe(this) { response ->
            if (response.isSuccessful) {
                Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Error deleting review: ${response.message()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayReviewDetails(review: ReviewDetails) {
        with(binding) {
            tvMovieTitle.text = review.movieTitle.trim().replace("\n", " ")
            tvReviewText.text = review.reviewText
            tvRating.text = formatRating(review.rating)
            review.likes.size.toString().also { tvLikeCount.text = it }
            tvName.text = review.author.username
            tvTags.text = review.tags.joinToString(", ") { tag ->
                if (tag.startsWith("#")) tag else "#$tag"
            }
            val userId = SharedPrefManager.getUserId(this@ReviewActivity)
            isLiked = review.likes.contains(userId)
            binding.ivLikeIcon.setImageResource(if (isLiked) R.drawable.ic_like_on else R.drawable.ic_like_off)
            totalLikes = review.likes.size
            updateProfileImage(review.author.profileImageUrl)
            updateEditVisibility(review.author.username)

            binding.llUser.setOnClickListener { navigateToUserActivity() }
            binding.tvShare.setOnClickListener { shareReview(review) }
        }
    }

    private fun formatRating(rating: Int): String {
        val validRating = rating.coerceIn(0, 5)
        return "★".repeat(validRating) + "☆".repeat(5 - validRating)
    }

    private fun updateProfileImage(url: String?) {
        if (!url.isNullOrEmpty()) {
            Picasso.get().load(url).placeholder(R.drawable.ic_account).error(R.drawable.ic_account)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_account)
        }
    }

    private fun updateEditVisibility(username: String) {
        binding.txtEdit.visibility =
            if (username == SharedPrefManager.getUsername(this)) View.VISIBLE else View.GONE
    }

    private fun handleLikeAction() {
        val likeRequest = LikeReviewRequest(reviewId, userId)
        viewModel.likeReview(likeRequest)
    }

    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_review, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val movieTitleEditText = dialogView.findViewById<EditText>(R.id.movieTitleEditText)
        val reviewTextEditText = dialogView.findViewById<EditText>(R.id.reviewTextEditText)
        val ratingSeekBar = dialogView.findViewById<RatingBar>(R.id.ratingSeekBar)
        val tagsEditText = dialogView.findViewById<TextView>(R.id.tagsEditText)
        val uploadReviewButton = dialogView.findViewById<Button>(R.id.uploadReviewButton)

        populateEditDialog(movieTitleEditText, reviewTextEditText, ratingSeekBar, tagsEditText)

        uploadReviewButton.setOnClickListener {
            val updatedRequest = collectUpdatedReview(
                movieTitleEditText,
                reviewTextEditText,
                ratingSeekBar,
                tagsEditText
            )
            if (updatedRequest != null) {
                token?.let {
                    viewModel.updateReview(it, reviewId, updatedRequest)
                    recreate()
                }
                dialog.dismiss()
            }
        }

        dialog.setOnCancelListener {
            dialog.dismiss()
            startActivity(Intent(this@ReviewActivity, ReviewActivity::class.java))
            finish()
        }
        dialog.show()
    }

    private fun populateEditDialog(
        movieTitleEditText: EditText,
        reviewTextEditText: EditText,
        ratingSeekBar: RatingBar,
        tagsEditText: TextView
    ) {
        movieTitleEditText.setText(binding.tvMovieTitle.text.toString().replace("\n", " ").trim())
        reviewTextEditText.setText(binding.tvReviewText.text.toString())
        ratingSeekBar.rating = 0f
        tagsEditText.text = binding.tvTags.text
    }

    private fun collectUpdatedReview(
        movieTitleEditText: EditText,
        reviewTextEditText: EditText,
        ratingSeekBar: RatingBar,
        tagsEditText: TextView
    ): ReviewRequest? {
        val updatedTitle = movieTitleEditText.text.toString()
        val updatedReviewText = reviewTextEditText.text.toString()
        val updatedRating = ratingSeekBar.rating.toInt()
        val updatedTags = tagsEditText.text.toString().split(",").map { it.trim() }

        return if (updatedTitle.isNotEmpty() && updatedReviewText.isNotEmpty()) {
            ReviewRequest(updatedTitle, updatedReviewText, updatedRating, updatedTags)
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review?")
            .setPositiveButton("Yes") { _, _ -> deleteReview() }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    private fun deleteReview() {
        token?.let { viewModel.deleteReview(it, reviewId) }
    }

    private fun navigateToUserActivity() {
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
    }

    private fun shareReview(review: ReviewDetails) {
        val shareMessage = buildShareMessage(review)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Review via"))
    }

    private fun buildShareMessage(review: ReviewDetails): String {
        return """
            Check out this review on Critix:
            Movie Title: ${review.movieTitle}
            Rating: ${review.rating}/5
            Tags: ${review.tags.joinToString(", ") { "#$it" }}

            Review: ${review.reviewText}
        """.trimIndent()
    }
}
