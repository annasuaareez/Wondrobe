package com.example.wondrobe.ui.wardrobe.Outfit

import android.content.pm.ActivityInfo
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
    private lateinit var userId: String
    private lateinit var clothesAdapter: AllClothesAdapter
    private val selectedImagesMap = mutableMapOf<String, ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_outfit)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val recyclerView = findViewById<RecyclerView>(R.id.clothesOutfitRecyclerView)
        val closeIcon = findViewById<ImageView>(R.id.closeIconAddOutfit)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        userId = intent.getStringExtra("USER_ID").toString()

        setupCloseIcon(closeIcon)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        clothesAdapter = AllClothesAdapter(emptyList()) { selectedClothes ->
            selectedClothes.imageUrl?.let { imageUrl ->
                selectedClothes.typeClothes?.let { placeClothesInContainer(imageUrl, it) }
            }
        }
        recyclerView.adapter = clothesAdapter

        // Obtener datos de Firebase Clothes
        loadUserClothes()
    }

    private fun setupCloseIcon(closeIcon: ImageView) {
        closeIcon.setOnClickListener {
            finish()
        }
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


    private val clothesContainerMap = mapOf(
        "Jackets" to R.id.container1,
        "Sweaters" to R.id.container2,
        "T-shirts" to R.id.container3,
        "Jeans" to R.id.container4,
        "Sneakers" to R.id.container5,
        "Scarves" to R.id.container6,
        "Jewelry" to R.id.container7,
        "Handbag" to R.id.container8,
        "Belts" to R.id.container9
    )

    /*private fun placeClothesInContainer(typeClothes: String) {
        Log.d("AddOutfit", "Placing clothes in container: $typeClothes")
        when (typeClothes) {
            "Jackets", "Coats" -> {
                Log.d("AddOutfit", "Placing in container1")
                placeImageInContainer(R.id.container1)
            }
            "Sweaters" -> {
                Log.d("AddOutfit", "Placing in container2")
                placeImageInContainer(R.id.container2)
            }
            "T-shirts", "Blouses" -> {
                Log.d("AddOutfit", "Placing in container3")
                placeImageInContainer(R.id.container3)
            }
            "Jeans", "Leggings", "Pants", "Shorts" -> {
                Log.d("AddOutfit", "Placing in container4")
                placeImageInContainer(R.id.container4)
            }
            "Sneakers", "Boots", "Sandals" -> {
                Log.d("AddOutfit", "Placing in container5")
                placeImageInContainer(R.id.container5)
            }
            "Scarves", "Hats" -> {
                Log.d("AddOutfit", "Placing in container6")
                placeImageInContainer(R.id.container6)
            }
            "Jewelry" -> {
                Log.d("AddOutfit", "Placing in container7")
                placeImageInContainer(R.id.container7)
            }
            "Handbag" -> {
                Log.d("AddOutfit", "Placing in container8")
                placeImageInContainer(R.id.container8)
            }
            "Belts" -> {
                Log.d("AddOutfit", "Placing in container9")
                placeImageInContainer(R.id.container9)
            }
            else -> {
                Log.d("AddOutfit", "Unknown type of clothes")
            }
        }
    }*/
    private fun placeClothesInContainer(imageUrl: String, typeClothes: String) {
        Log.d("AddOutfit", "Placing clothes in container: $typeClothes")
        // Buscar el contenedor asociado al tipo de ropa
        val containerId = clothesContainerMap[typeClothes]
        containerId?.let { id ->
            val container = findViewById<ImageView>(id)
            // Cargar la imagen directamente en el contenedor
            Glide.with(this)
                .load(imageUrl)
                .into(container)
        }
    }

}
