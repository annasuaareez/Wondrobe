package com.example.wondrobe.adapters

import android.content.Context
import android.widget.ArrayAdapter
import com.example.wondrobe.ui.add.clothes.AddClothes

class ClothesAdapter<T>(context: Context, resource: Int, objects: Array<T>) :
    ArrayAdapter<T>(context, resource, objects) {

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): T? {
        return super.getItem(position)
    }

    override fun getCount(): Int {
        return super.getCount()
    }
}
