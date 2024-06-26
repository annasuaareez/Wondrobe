package com.example.wondrobe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.wondrobe.R

class ImageFollowAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    private val postIds: List<String>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ImageFollowAdapter.ViewHolder>() {

    interface OnImageClickListener {
        fun onImageClick(postId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        val postId = postIds[position]

        Glide.with(context)
            .load(imageUrl)
            .transform(FitCenter(), RoundedCorners(33))
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            listener.onImageClick(postId)
        }

        holder.imageView.adjustViewBounds = true
        holder.imageView.scaleType = ImageView.ScaleType.FIT_XY
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
