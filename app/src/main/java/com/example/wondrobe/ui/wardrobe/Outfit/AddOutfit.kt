package com.example.wondrobe.ui.wardrobe.Outfit

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.example.wondrobe.adapters.AllClothesAdapter
import com.example.wondrobe.data.Clothes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddOutfit : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var clothesAdapter: AllClothesAdapter
    private lateinit var userId: String
    private lateinit var canvasImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_outfit)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userId = intent.getStringExtra("USER_ID").toString()

        canvasImageView = findViewById(R.id.canvas)

        val recyclerView = findViewById<RecyclerView>(R.id.clothesOutfitRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        clothesAdapter = AllClothesAdapter(emptyList()) { selectedClothes ->
            selectedClothes.imageUrl?.let { loadClothesImage(it) }
        }
        recyclerView.adapter = clothesAdapter

        // Obtener datos de Firebase Clothes
        loadUserClothes()
    }

    private fun loadUserClothes() {
        Log.d("AddOutfit", "Fetching clothes for user: $userId")
        firestore.collection("clothes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val clothesList = mutableListOf<Clothes>()
                for (document in result) {
                    val clothes = document.toObject(Clothes::class.java)
                    clothesList.add(clothes)
                }
                Log.d("AddOutfit", "Fetched ${clothesList.size} items")
                // Agregar datos al Adaptador
                clothesAdapter.setItems(clothesList)
            }
            .addOnFailureListener { exception ->
                Log.e("AddOutfit", "Error fetching clothes", exception)
            }
    }

    private fun loadClothesImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(canvasImageView)
    }
}
