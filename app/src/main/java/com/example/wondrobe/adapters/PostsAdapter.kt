package com.example.wondrobe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.Post

class PostsAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val postUsername: TextView = itemView.findViewById(R.id.postUsername)
        val postTitle: TextView = itemView.findViewById(R.id.postTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        Glide.with(holder.postImage.context)
            .load(post.imageUrl)
            .into(holder.postImage)
        holder.postUsername.text = post.userId
        holder.postTitle.text = post.title
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}
