package com.example.wondrobe.ui.save

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.wondrobe.R
import com.example.wondrobe.data.SavedPost

class SaveAdapter(private val savedPosts: List<SavedPost>) : RecyclerView.Adapter<SaveAdapter.SaveViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_post, parent, false)
        return SaveViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaveViewHolder, position: Int) {
        val savedPost = savedPosts[position]
        val imageUrl = savedPost.imageUrl ?: ""

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.postImage)
    }

    override fun getItemCount(): Int = savedPosts.size

    class SaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.savedPostImage)
    }
}
