package com.example.wondrobe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.Clothes

class AllClothesAdapter(
    private var clothesList: List<Clothes>
) : RecyclerView.Adapter<AllClothesAdapter.ClothesViewHolder>() {

    class ClothesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clothesImage: ImageView = itemView.findViewById(R.id.allClothes_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_allclothes, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        val clothes = clothesList[position]
        Glide.with(holder.clothesImage.context)
            .load(clothes.imageUrl)
            .into(holder.clothesImage)
    }

    override fun getItemCount() = clothesList.size

    fun setItems(items: List<Clothes>) {
        clothesList = items
        notifyDataSetChanged()
    }
}
