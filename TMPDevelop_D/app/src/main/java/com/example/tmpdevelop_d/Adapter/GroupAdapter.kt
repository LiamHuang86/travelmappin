package com.example.tmpdevelop_d.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.Users.Group


class GroupAdapter(private val groups: List<Group>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val groupNameTextView: TextView = itemView.findViewById(R.id.group_name)
        val photoUrlImageView: ImageView = itemView.findViewById(R.id.group_image)
        val memberCountTextView: TextView = itemView.findViewById(R.id.group_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(group)
        }
        holder.groupNameTextView.text = group.groupName
        holder.memberCountTextView.text = group.totalMembers.toString()
        Glide.with(holder.itemView.context)
            .load(group.photoUrl)
            .placeholder(R.drawable.default_avatar)
            .circleCrop()
            .into(holder.photoUrlImageView)
    }
    interface ItemClickListener {
        fun onItemClick(group: Group)
    }
    override fun getItemCount(): Int {
        return groups.size
    }
}