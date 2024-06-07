package com.example.wondrobe.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.Post
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat

class PostAdapter(
    private val context: Context,
    private var posts: List<Post>,
    private val listener: OnPostClickListener,
    private val isPostSaved: (String, (Boolean) -> Unit) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnPostClickListener {
        fun onPostClick(post: Post)
        fun onSaveClick(post: Post, saveIconView: ImageView)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.post_image)
        val postTitle: TextView = itemView.findViewById(R.id.post_title)
        val postDescription: TextView = itemView.findViewById(R.id.post_description)
        val saveIcon: ImageView = itemView.findViewById(R.id.saveIconPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.postTitle.text = post.title
        holder.postDescription.text = post.description
        Glide.with(context).load(post.imageUrl).into(holder.postImage)

        post.postId?.let {
            isPostSaved(it) { isSaved ->
                updateSaveIcon(holder.saveIcon, isSaved)
            }
        }

        holder.itemView.setOnClickListener {
            listener.onPostClick(post)
        }

        holder.saveIcon.setOnClickListener {
            listener.onSaveClick(post, holder.saveIcon)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun updateSaveIcon(saveIconView: ImageView, isSaved: Boolean) {
        if (isSaved) {
            saveIconView.setImageResource(R.drawable.ic_save)
        } else {
            saveIconView.setImageResource(R.drawable.ic_unsave)
        }
    }
}
