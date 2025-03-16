package com.videomate.critix2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.videomate.critix.R
import com.videomate.critix2.model.UserPost

class UserPostsAdapter(
    private val posts: List<UserPost>,private val onReviewClick: (UserPost) -> Unit
) : RecyclerView.Adapter<UserPostsAdapter.UserPostViewHolder>() {

    class UserPostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvMovieTitle)
        val review: TextView = view.findViewById(R.id.tvReviewText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return UserPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserPostViewHolder, position: Int) {
        val post = posts[position]
        holder.title.text = post.title.replace("\n", " ").trim()
        holder.review.text = post.review
        holder.itemView.setOnClickListener{
            onReviewClick(post)
        }
    }

    override fun getItemCount(): Int = posts.size
}
