package com.example.wondrobe.ui.wardrobe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wondrobe.adapters.ClothesOptionsAdapter
import com.example.wondrobe.data.Clothes
import com.example.wondrobe.databinding.FragmentWardrobeBinding
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore


class WardrobeFragment : Fragment() {
    private var _binding: FragmentWardrobeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var userId: String
    private lateinit var clothesAdapter: ClothesOptionsAdapter
    private val clothesList = mutableListOf<Clothes>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userId = UserUtils.getUserId(requireContext()).toString()

        Log.e("WardrobeFragment", "UID del usuario: $userId")

        setupRecyclerView()
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

    private fun loadClothes() {
        userId = UserUtils.getUserId(requireContext()).toString()

        Log.e("UserFragment", "UID del usuario: $userId")

        val db = FirebaseFirestore.getInstance()
        val clothesCollection = db.collection("clothes")

        clothesCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
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
            .addOnFailureListener {
                binding.textNoClothes.visibility = View.VISIBLE
                binding.recyclerViewClothes.visibility = View.GONE
            }
    }

}
