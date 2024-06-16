package com.example.wondrobe.ui.wardrobe.details

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wondrobe.adapters.ClothesAdapter
import com.example.wondrobe.data.Clothes
import com.example.wondrobe.databinding.ActivityCategoryDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore

class CategoryDetails : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryDetailsBinding
    private lateinit var clothesAdapter: ClothesAdapter
    private var clothesList = mutableListOf<Clothes>()
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val categoryName = intent.getStringExtra("categoryName")
        val clothes = intent.getParcelableArrayListExtra<Clothes>("clothesList")

        binding.categoryTitle.text = categoryName
        clothesList.addAll(clothes ?: emptyList())

        setupBackButton()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        clothesAdapter = ClothesAdapter(clothesList) { clothes ->
            deleteClothes(clothes)
        }
        binding.recyclerViewClothes.apply {
            layoutManager = GridLayoutManager(this@CategoryDetails, 2)
            adapter = clothesAdapter
        }
    }

    private fun setupBackButton() {
        binding.backIconClothesDetailsActivity.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteClothes(clothes: Clothes) {
        clothes.userId?.let { userId ->
            firestore.collection("clothes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("imageUrl", clothes.imageUrl)
                .whereEqualTo("title", clothes.title)
                .whereEqualTo("typeClothes", clothes.typeClothes)
                .whereEqualTo("date", clothes.date)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("clothes").document(document.id).delete()
                            .addOnSuccessListener {
                                val position = clothesList.indexOf(clothes)
                                if (position != -1) {
                                    clothesList.removeAt(position)
                                    clothesAdapter.notifyItemRemoved(position)
                                    clothesAdapter.notifyDataSetChanged()
                                }
                            }
                    }
                }
        }
    }
}