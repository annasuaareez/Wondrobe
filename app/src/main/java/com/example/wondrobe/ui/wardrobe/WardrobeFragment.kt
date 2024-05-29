package com.example.wondrobe.ui.wardrobe

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.wondrobe.R
import com.example.wondrobe.adapters.ClothesCategoryAdapter
import com.example.wondrobe.data.Clothes
import com.example.wondrobe.data.ClothesCategory
import com.example.wondrobe.databinding.FragmentWardrobeBinding
import com.example.wondrobe.ui.wardrobe.details.CategoryDetails
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class WardrobeFragment : Fragment() {
    private var _binding: FragmentWardrobeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userId: String
    private lateinit var categoryAdapter: ClothesCategoryAdapter
    private val categories = mutableListOf<ClothesCategory>()
    private var selectedType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        userId = UserUtils.getUserId(requireContext()).toString()

        val clothesButton = binding.clothesButton
        val outfitButton = binding.outfitsButton
        val clothesIndicator = binding.clothesIndicator
        val outfitIndicator = binding.outfitIndicator

        setupRecyclerView()
        setupSpinner()
        loadClothes()

        clothesButton.setOnClickListener {
            animateIndicatorChange(clothesIndicator, outfitIndicator)
            showClothesContent()
        }

        outfitButton.setOnClickListener {
            animateIndicatorChange(outfitIndicator, clothesIndicator)
            showOutfitContent()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        categoryAdapter = ClothesCategoryAdapter(categories) { category ->
            showClothesOfCategory(category)
        }
        binding.recyclerViewClothes.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = categoryAdapter
        }
    }

    private fun setupSpinner() {
        val options = arrayOf(
            "All", "T-shirts", "Sweaters", "Blouses", "Jeans", "Leggings", "Pants", "Shorts", "Dress", "Jackets", "Coats",
            "Scarves", "Hats", "Jewelry", "Handbag", "Belts", "Sneakers", "Boots", "Sandals"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //binding.spinnerFilterClothes.adapter = adapter

        /*binding.spinnerFilterClothes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedType = if (position == 0) null else options[position]
                loadClothes()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedType = null
                loadClothes()
            }
        }*/
    }

    private fun animateIndicatorChange(showIndicator: View, hideIndicator: View) {
        val hideAnimator = ObjectAnimator.ofFloat(hideIndicator, "alpha", 1f, 0f)
        hideAnimator.duration = 250 // Duration of the animation in milliseconds
        hideAnimator.interpolator = AccelerateDecelerateInterpolator()

        val showAnimator = ObjectAnimator.ofFloat(showIndicator, "alpha", 0f, 1f)
        showAnimator.duration = 250 // Duration of the animation in milliseconds
        showAnimator.interpolator = AccelerateDecelerateInterpolator()

        hideAnimator.addUpdateListener {
            if (hideIndicator.alpha == 0f) {
                hideIndicator.visibility = View.INVISIBLE
            }
        }

        showAnimator.addUpdateListener {
            if (showIndicator.alpha == 1f) {
                showIndicator.visibility = View.VISIBLE
            }
        }

        hideAnimator.start()
        showAnimator.start()
    }

    private fun showClothesContent() {
        //binding.spinnerFilterClothes.visibility = View.VISIBLE
        binding.recyclerViewClothes.visibility = if (categories.isEmpty()) View.GONE else View.VISIBLE
        binding.textNoClothes.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showOutfitContent() {
        //binding.spinnerFilterClothes.visibility = View.GONE
        binding.recyclerViewClothes.visibility = View.GONE
        binding.textNoClothes.visibility = View.VISIBLE
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
            val groupedClothes = result.documents.map { it.toObject(Clothes::class.java)!! }
                .groupBy { it.typeClothes }

            categories.clear()
            categories.add(ClothesCategory("All", groupedClothes.flatMap { it.value }))
            groupedClothes.forEach { (type, clothes) ->
                categories.add(ClothesCategory(type ?: "Unknown", clothes))
            }
            categoryAdapter.notifyDataSetChanged()
            binding.textNoClothes.visibility = View.GONE
            binding.recyclerViewClothes.visibility = View.VISIBLE
        }
    }

    private fun showClothesOfCategory(category: ClothesCategory) {
        val intent = Intent(requireContext(), CategoryDetails::class.java).apply {
            putExtra("categoryName", category.categoryName)
            putParcelableArrayListExtra("clothesList", ArrayList(category.clothesList))
        }
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

}
