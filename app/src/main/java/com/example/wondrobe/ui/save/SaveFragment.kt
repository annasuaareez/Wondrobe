package com.example.wondrobe.ui.save

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.wondrobe.data.SavedPost
import com.example.wondrobe.databinding.FragmentSaveBinding
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore

class SaveFragment : Fragment() {

    private var _binding: FragmentSaveBinding? = null
    private lateinit var adapter: SaveAdapter
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        loadSavedPosts()
    }

    private fun loadSavedPosts() {
        val userId = UserUtils.getUserId(requireContext()).toString()
        Log.e("UserID", userId)

        db.collection("users").document(userId).collection("savedPosts")
            .get()
            .addOnSuccessListener { documents ->
                val savedPosts = documents.mapNotNull { document ->
                    val postId = document.getString("postId")
                    val imageUrl = document.getString("imageUrl")
                    if (postId != null && imageUrl != null) {
                        SavedPost(postId, imageUrl)
                    } else {
                        null
                    }
                }
                if (savedPosts.isNotEmpty()) {
                    adapter = SaveAdapter(savedPosts)
                    binding.saveRecyclerView.adapter = adapter
                } else {
                    Log.d("SaveFragment", "No saved posts found")
                    // Mostrar un mensaje indicando que no hay posts guardados
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SaveFragment", "Error fetching saved posts: ${exception.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
