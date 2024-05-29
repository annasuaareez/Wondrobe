package com.example.wondrobe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.ClothesCategory

class ClothesCategoryAdapter(
    private val categories: List<ClothesCategory>,
    private val onCategoryClick: (ClothesCategory) -> Unit
) : RecyclerView.Adapter<ClothesCategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.category_name)
        val categoryImage: ImageView = view.findViewById(R.id.category_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.categoryName.text = category.categoryName
        if (category.clothesList.isNotEmpty()) {
            val firstClothesImageUrl = category.clothesList[0].imageUrl

            Glide.with(holder.categoryImage.context)
                .load(firstClothesImageUrl)
                .placeholder(R.drawable.ic_wardrobe)
                .into(holder.categoryImage)
        } else {
            holder.categoryImage.setImageResource(R.drawable.ic_wardrobe)
        }
        holder.itemView.setOnClickListener { onCategoryClick(category) }
    }

    override fun getItemCount() = categories.size
}