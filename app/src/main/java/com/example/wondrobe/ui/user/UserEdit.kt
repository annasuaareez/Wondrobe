package com.example.wondrobe.ui.user

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.wondrobe.databinding.ActivityUserEditBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserEdit : AppCompatActivity() {

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private lateinit var biography: String
    private lateinit var photoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserDetails()

        binding.saveChange.setOnClickListener {
            val newUsername = binding.newUsernameEditText.text.toString()
            val newFirstName = binding.newNameEditText.text.toString()
            val newBiography = binding.newBiographyEditText.text.toString()

            if (validateInputs(newUsername, newFirstName, newBiography)) {
                // Verificar si el nuevo nombre de usuario es diferente al nombre de usuario actual
                if (newUsername != username) {
                    // El usuario desea cambiar su nombre de usuario
                    saveUserData(newUsername, newFirstName, newBiography)
                } else {
                    // El usuario no desea cambiar su nombre de usuario, solo actualiza la información adicional
                    saveAdditionalUserData(newFirstName, newBiography)
                }
            }
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
                    biography = document.getString("biography") ?: ""
                    photoUrl = document.getString("profileImage") ?: ""

                    // Actualizar los EditText con los valores recuperados
                    binding.newNameEditText.setText(firstName)
                    binding.newUsernameEditText.setText(username)
                    binding.newBiographyEditText.setText(biography)

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

    private fun validateInputs(username: String, firstName: String, biography: String): Boolean {
        if (username.isBlank() || firstName.isBlank()) {
            //Toast.makeText(this, "Username and First Name cannot be empty.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.length > 20) {
            //Toast.makeText(this, "Username cannot exceed 40 characters.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (firstName.length > 40) {
            //Toast.makeText(this, "First Name cannot exceed 40 characters.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (biography.length > 160) {
            //Toast.makeText(this, "Biography cannot exceed 160 characters.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserData(username: String, firstName: String, biography: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // El nombre de usuario no está registrado, se puede guardar
                    val user = hashMapOf<String, Any>(
                        "username" to username,
                        "firstName" to firstName,
                        "biography" to biography
                        // Agrega aquí los demás campos que desees actualizar
                    )

                    usersCollection.document(userId)
                        .update(user)
                        .addOnSuccessListener {
                            showAlertToast("User data updated successfully.")
                            val resultIntent = Intent()
                            resultIntent.putExtra("username", username)
                            resultIntent.putExtra("firstName", firstName)
                            resultIntent.putExtra("biography", biography)
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showAlertToast("Error updating user data: ${e.message}")
                        }
                } else {
                    // El nombre de usuario ya está registrado
                    showAlertDialog("Username already exists. Please choose a different one.")
                }
            }
            .addOnFailureListener {e ->
                showAlertToast("Error checking username availability: ${e.message}")
            }
    }

    private fun saveAdditionalUserData(firstName: String, biography: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        val user = hashMapOf<String,Any>(
            "firstName" to firstName,
            "biography" to biography
            // Agrega aquí los demás campos que desees actualizar
        )

        usersCollection.document(userId)
            .update(user)
            .addOnSuccessListener {
                showAlertToast("User data updated successfully.")
                val resultIntent = Intent()
                resultIntent.putExtra("username", username)
                resultIntent.putExtra("firstName", firstName)
                resultIntent.putExtra("biography", biography)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                showAlertToast("Error updating user data: ${e.message}")
            }
    }

    private fun showAlertDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Invalid credentials")
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
