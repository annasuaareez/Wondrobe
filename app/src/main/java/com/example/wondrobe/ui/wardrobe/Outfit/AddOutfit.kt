package com.example.wondrobe.ui.wardrobe.Outfit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_outfit)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userId = intent.getStringExtra("USER_ID").toString()

        val recyclerView = findViewById<RecyclerView>(R.id.clothesOutfitRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)
        clothesAdapter = AllClothesAdapter(emptyList())
        recyclerView.adapter = clothesAdapter

        // Obtener datos de Firebase y mostrarlos en RecyclerView
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
                // Agregar datos al Adaptear
                clothesAdapter.setItems(clothesList)
            }
            .addOnFailureListener { exception ->
                Log.e("AddOutfit", "Error fetching clothes", exception)
            }
    }
}
