package com.videomate.critix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.videomate.critix.R
import com.videomate.critix.model.User

class UserAdapter(private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val userList = mutableListOf<User>()
    private val connectedUsers = mutableSetOf<String>() // Track connected users by ID

    fun setUsers(users: List<User>) {
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }

    fun updateConnectionStatus(userId: String, isConnected: Boolean) {
        if (isConnected) connectedUsers.add(userId) else connectedUsers.remove(userId)
        notifyItemChanged(userList.indexOfFirst { it.id == userId })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.tvUsername)
        private val profileImageView: ImageView = itemView.findViewById(R.id.ivProfileImage)
        private val connectButton: Button = itemView.findViewById(R.id.connectButton)

        fun bind(user: User) {
            usernameTextView.text = user.username
            if (!user.profileImageUrl.isNullOrEmpty()) {
                val imageUrl = user.profileImageUrl.takeIf { it.isNotEmpty() }
                    ?.replace("undefined/", "")
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_account)
                    .error(R.drawable.ic_account)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.ic_account)
            }

            // Update button appearance based on connection status
            if (connectedUsers.contains(user.id)) {
                connectButton.setBackgroundColor(itemView.context.getColor(R.color.cream))
                connectButton.setTextColor(itemView.context.getColor(R.color.black))
                connectButton.text = "Connected"
            } else {
                connectButton.setBackgroundColor(itemView.context.getColor(R.color.maroon))
                connectButton.setTextColor(itemView.context.getColor(R.color.cream))
                connectButton.text = "Connect"

            }

            // Handle button clicks
            connectButton.setOnClickListener {
                connectButton.setBackgroundColor(itemView.context.getColor(R.color.cream))
                connectButton.setTextColor(itemView.context.getColor(R.color.black))
                connectButton.text = "Connected"
                itemClickListener.onButtonClick(user)
            }

            // Handle item click
            itemView.setOnClickListener {
                itemClickListener.onItemClick(user)
            }


        }
    }

    // Define the interface
    interface OnItemClickListener {
        fun onItemClick(user: User)
        fun onButtonClick(user: User)
    }
}
