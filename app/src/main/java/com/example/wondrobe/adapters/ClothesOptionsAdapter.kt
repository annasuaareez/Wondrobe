package com.example.wondrobe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.data.Clothes
import com.example.wondrobe.databinding.ItemClothesBinding

class ClothesOptionsAdapter(private val clothesList: List<Clothes>) : RecyclerView.Adapter<ClothesOptionsAdapter.ClothesOptionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesOptionsViewHolder {
        val binding = ItemClothesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClothesOptionsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClothesOptionsViewHolder, position: Int) {
        holder.bind(clothesList[position])
    }

    override fun getItemCount() = clothesList.size

    class ClothesOptionsViewHolder(private val binding: ItemClothesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(clothes: Clothes) {
            binding.textClothesName.text = clothes.name

            Glide.with(binding.imageClothes.context)
                .load(clothes.imageUrl)
                .into(binding.imageClothes)
        }
    }

}