package com.example.wondrobe.ui.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    private lateinit var biography: String
    private lateinit var photoUrl: String
    private lateinit var bannerUrl: String
    private lateinit var followersCount: String
    private lateinit var followingCount: String
    private var userDetailsLoaded = false // Indica si los detalles del usuario ya han sido cargados

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.editProfileButton.setOnClickListener {
            redirectToEditUser()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        // Actualizar los detalles del usuario cada vez que se muestre el fragmento
        if (!userDetailsLoaded) {
            loadUserDetails()
        } else {
            updateUI()
        }
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
                    biography = document.getString("biography") ?: ""
                    photoUrl = document.getString("profileImage") ?: ""
                    bannerUrl = document.getString("bannerImage") ?: ""
                    followersCount = document.getLong("followersCount")?.toString() ?: "0"
                    followingCount = document.getLong("followingCount")?.toString() ?: "0"

                    // Indicar que los detalles del usuario han sido cargados
                    userDetailsLoaded = true

                    // Actualizar la interfaz de usuario incluyendo la foto
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
        binding.textViewBiography.text = biography
        binding.textViewFollowersCount.text = followersCount
        binding.textViewFollowingCount.text = followingCount

        //showAlertDialog(photoUrl)
        // Verificar si el usuario tiene una foto de perfil
        if (photoUrl.isNotEmpty()) {
            // Cargar la imagen de perfil y aplicar la máscara circular
            Glide.with(requireContext()).clear(binding.circle)
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

            // Forzar la actualización de la vista después de cargar la imagen
            binding.circle.invalidate()
        }

        // Verificar si el usuario tiene un banner
        if (bannerUrl.isNotEmpty()) {
            // Cargar la imagen de perfil y aplicar la máscara circular
            Glide.with(requireContext()).clear(binding.banner)
            Glide.with(requireContext())
                .load(bannerUrl)
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
                .into(binding.banner)

            // Forzar la actualización de la vista después de cargar la imagen
            binding.banner.invalidate()
        }

        // Mostrar los TextView una vez que se han cargado los datos del usuario
        binding.textViewName.visibility = View.VISIBLE
        binding.textViewUsername.visibility = View.VISIBLE
        binding.textViewBiography.visibility = View.VISIBLE
    }

    private fun redirectToEditUser() {
        val intent = Intent(activity, UserEdit::class.java)
        startActivityForResult(intent, REQUEST_CODE_EDIT_USER)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        // Notificar a la actividad para que elimine este fragmento
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT_USER && resultCode == Activity.RESULT_OK) {
            loadUserDetails()
        }
    }

    companion object {
        private const val REQUEST_CODE_EDIT_USER = 1001
    }
}
