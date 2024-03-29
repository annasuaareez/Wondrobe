package com.example.wondrobe.ui.user

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.wondrobe.R
import com.example.wondrobe.databinding.ActivityUserEditBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserEdit : AppCompatActivity() {

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private lateinit var photoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserDetails()

        binding.saveChange.setOnClickListener {
            // Aquí puedes agregar la lógica para guardar los cambios en la base de datos
        }
    }

    private fun loadUserDetails() {
        userId = getSharedPreferences("user_data", MODE_PRIVATE)
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

                    // Actualizar los EditText con los valores recuperados
                    binding.newNameEditText.setText(firstName)
                    binding.newUsernameEditText.setText(username)

                    // Cargar la imagen de perfil si existe
                    if (photoUrl.isNotEmpty()) {
                        // Cargar la imagen de perfil y aplicar la máscara circular
                        Glide.with(this)
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
                            .into(binding.editUserCircle)
                    }
                } else {
                    showAlertToast("User details not found.")
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error fetching user details: ${e.message}")
            }
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
