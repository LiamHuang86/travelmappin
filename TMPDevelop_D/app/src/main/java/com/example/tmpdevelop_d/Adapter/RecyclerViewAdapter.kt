package com.example.tmpdevelop_d.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Users

class RecyclerViewAdapter (private val userList: ArrayList<Users>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView = itemView.findViewById(R.id.friend_name)
        var userID: TextView = itemView.findViewById(R.id.friend_id)
        var imageUrl: ImageView = itemView.findViewById(R.id.friend_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.username.text = user.username
        holder.userID.text = user.userID
        Glide.with(holder.itemView.context)
            .load(user.imageUrl ?: "https://example.com/placeholder.jpg")
            .circleCrop()
            .into(holder.imageUrl)
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}