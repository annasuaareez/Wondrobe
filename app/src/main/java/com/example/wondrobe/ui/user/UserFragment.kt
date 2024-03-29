package com.example.wondrobe.ui.user

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.wondrobe.R
import com.example.wondrobe.databinding.FragmentUserBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private lateinit var photoUrl: String
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

        binding.editProfileButton.setOnClickListener {
            redirectToEditUser()
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
                    photoUrl = document.getString("profileImage") ?: ""

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

        //showAlertDialog(photoUrl)
        // Verificar si el usuario tiene una foto de perfil
        if (photoUrl.isNotEmpty()) {
            // Cargar la imagen de perfil y aplicar la máscara circular
            Glide.with(requireContext())
                .load(photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Manejar el error al cargar la imagen
                        //showAlertToast("Error loading image: ${e?.message}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        //showAlertToast("Imagen carga bien")
                        return false
                    }

                })
                .transform(CircleCrop())
                .into(binding.circle)
        }

        // Mostrar los TextView una vez que se han cargado los datos del usuario
        binding.textViewName.visibility = View.VISIBLE
        binding.textViewUsername.visibility = View.VISIBLE
    }

    private fun redirectToEditUser() {
        val intent = Intent(activity, UserEdit::class.java)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
