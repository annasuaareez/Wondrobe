package com.example.wondrobe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.data.Clothes

class ClothesAdapter(
    private val clothesList: MutableList<Clothes>,
    private val onDelete: (Clothes) -> Unit
) : RecyclerView.Adapter<ClothesAdapter.ClothesViewHolder>() {

    class ClothesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val clothesImage: ImageView = view.findViewById(R.id.clothes_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clothes, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        val clothes = clothesList[position]
        Glide.with(holder.clothesImage.context)
            .load(clothes.imageUrl)
            .placeholder(R.drawable.ic_wardrobe)
            .into(holder.clothesImage)

        holder.itemView.setOnLongClickListener {
            showPopupMenu(holder.itemView, clothes)
            true
        }
    }

    private fun showPopupMenu(view: View, clothes: Clothes) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.menu_clothes_item, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete_clothes -> {
                    onDelete(clothes)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount() = clothesList.size
}
