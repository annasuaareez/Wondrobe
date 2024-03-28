package com.example.wondrobe.ui.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wondrobe.databinding.FragmentUserBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private var userDetailsLoaded = false // Indica si los detalles del usuario ya han sido cargados

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Verificar si los detalles del usuario ya se han cargado previamente
        if (!userDetailsLoaded) {
            loadUserDetails()
        } else {
            updateUI()
        }

        return root
    }

    private fun loadUserDetails() {
        userId = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
            .getString("user_id", "") ?: ""

        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    firstName = document.getString("firstName") ?: ""
                    username = document.getString("username") ?: ""

                    // Indicar que los detalles del usuario han sido cargados
                    userDetailsLoaded = true

                    updateUI()
                } else {
                    showAlertToast("User details not found.")
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error fetching user details: ${e.message}")
            }
    }

    private fun updateUI() {
        val formattedUsername = "@$username"
        binding.textViewName.text = firstName
        binding.textViewUsername.text = formattedUsername

        // Mostrar los TextView una vez que se han cargado los datos del usuario
        binding.textViewName.visibility = View.VISIBLE
        binding.textViewUsername.visibility = View.VISIBLE
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
