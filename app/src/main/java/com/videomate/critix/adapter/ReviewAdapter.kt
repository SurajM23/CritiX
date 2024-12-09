package com.videomate.critix.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.videomate.critix.databinding.ItemFeedBinding
import com.videomate.critix.model.Review2

class ReviewsAdapter(private val onReviewClick: (String) -> Unit ,  private val onAuthorClick : (String) -> Unit) :
    RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {

    private var reviews = mutableListOf<Review2>()

    inner class ReviewViewHolder(private val binding: ItemFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review2) {
            with(binding) {
                tvMovieTitle.text = review.movieTitle
                tvReviewText.text = review.reviewText
                tvName.text = review.author.username
                val rating = review.rating.coerceIn(0, 5) // Ensure rating is between 0 and 5
                val ratingStars = "★".repeat(rating) + "☆".repeat(5 - rating)
                tvRating.text = ratingStars
                tvTags.text = review.tags.joinToString(", ") { "#$it" }

                llUser.setOnClickListener {
                    onAuthorClick(review.author._id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
        holder.itemView.setOnClickListener {
            onReviewClick(reviews[position]._id) // Pass review ID to the callback
        }

    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review2>) {
        reviews.clear()
        reviews.addAll(newReviews)
        notifyDataSetChanged()
    }
}
