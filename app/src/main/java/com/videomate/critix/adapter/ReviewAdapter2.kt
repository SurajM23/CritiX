package com.videomate.critix.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.videomate.critix.databinding.ItemReviewBinding
import com.videomate.critix.model.Review2
import com.videomate.critix.utils.Constants

class ReviewAdapter2(
    private val reviews: List<Review2>,
    private val onReviewClick: (String) -> Unit // Callback for click events
) : RecyclerView.Adapter<ReviewAdapter2.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewBinding.inflate(inflater, parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(review)
        holder.itemView.setOnClickListener {
            Constants.USER_ID = review.author._id
            onReviewClick(review._id) // Pass review ID to the callback
        }
    }

    override fun getItemCount(): Int = reviews.size

    class ReviewViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review2) {
            binding.tvMovieTitle.text = review.movieTitle.replace("\n", " ").trim()
            binding.tvReviewText.text = review.reviewText
        }
    }
}
