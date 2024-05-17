package com.example.wondrobe.ui.save

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.wondrobe.databinding.FragmentSaveBinding
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore

class SaveFragment : Fragment() {

    private var _binding: FragmentSaveBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    private lateinit var adapter: SaveAdapter
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        loadSavedPosts()
    }

    private fun loadSavedPosts() {
        val userId = UserUtils.getUserId(requireContext()).toString()
        Log.e("UserID", userId)
        db.collection("postSave")
            .whereEqualTo("loggedInUserId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.documents
                adapter = SaveAdapter(posts)
                binding.saveRecyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                // Handle the error
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}