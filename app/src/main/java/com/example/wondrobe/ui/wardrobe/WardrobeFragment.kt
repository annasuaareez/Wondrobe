package com.example.wondrobe.ui.wardrobe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wondrobe.adapters.ClothesOptionsAdapter
import com.example.wondrobe.data.Clothes
import com.example.wondrobe.databinding.FragmentWardrobeBinding
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class WardrobeFragment : Fragment() {
    private var _binding: FragmentWardrobeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userId: String
    private lateinit var clothesAdapter: ClothesOptionsAdapter
    private val clothesList = mutableListOf<Clothes>()
    private var selectedType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userId = UserUtils.getUserId(requireContext()).toString()

        setupRecyclerView()
        setupSpinner()
        loadClothes()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        clothesAdapter = ClothesOptionsAdapter(clothesList)
        binding.recyclerViewClothes.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = clothesAdapter
        }
    }

    private fun setupSpinner() {
        val options = arrayOf(
            "All", "T-shirts", "Sweaters", "Blouses", "Jeans", "Leggings", "Pants", "Shorts", "Dress", "Jackets", "Coats",
            "Scarves", "Hats", "Jewelry", "Handbag", "Belts", "Sneakers", "Boots", "Sandals"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilterClothes.adapter = adapter

        binding.spinnerFilterClothes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedType = if (position == 0) null else options[position]
                loadClothes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedType = null
                loadClothes()
            }
        }
    }

    private fun loadClothes() {
        userId = UserUtils.getUserId(requireContext()).toString()
        val db = FirebaseFirestore.getInstance()
        val clothesCollection = db.collection("clothes")

        var query = clothesCollection.whereEqualTo("userId", userId)
        if (selectedType != null) {
            query = query.whereEqualTo("typeClothes", selectedType)
        }

        query.get()
            .addOnSuccessListener { result ->
                handleQueryResult(result)
            }
            .addOnFailureListener {
                binding.textNoClothes.visibility = View.VISIBLE
                binding.recyclerViewClothes.visibility = View.GONE
            }
    }

    private fun handleQueryResult(result: QuerySnapshot) {
        if (result.isEmpty) {
            binding.textNoClothes.visibility = View.VISIBLE
            binding.recyclerViewClothes.visibility = View.GONE
        } else {
            clothesList.clear()
            for (document in result) {
                val clothes = document.toObject(Clothes::class.java)
                clothesList.add(clothes)
            }
            clothesAdapter.notifyDataSetChanged()
            binding.textNoClothes.visibility = View.GONE
            binding.recyclerViewClothes.visibility = View.VISIBLE
        }
    }
}
