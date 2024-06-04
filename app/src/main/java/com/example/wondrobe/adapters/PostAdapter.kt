package com.example.wondrobe.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.Post
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private val context: Context,
    private var posts: List<Post>,
    private val listener: OnPostClickListener
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnPostClickListener {
        fun onPostClick(postId: String)
    }

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.post_image)
        private val titleView: TextView = itemView.findViewById(R.id.post_title_home)
        private val descriptionView: TextView = itemView.findViewById(R.id.post_description)

        fun bind(post: Post) {
            titleView.text = post.title
            descriptionView.text = post.description
            Glide.with(context).load(post.imageUrl).into(imageView)
            itemView.setOnClickListener {
                post.postId?.let { postId -> listener.onPostClick(postId) }
            }
        }
    }

}
