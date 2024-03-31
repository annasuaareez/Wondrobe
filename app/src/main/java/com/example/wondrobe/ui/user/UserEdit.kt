package com.example.wondrobe.ui.user

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.wondrobe.R
import com.example.wondrobe.databinding.ActivityUserEditBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class UserEdit : AppCompatActivity() {

    // Definir constantes para solicitar permisos
    private val CAMERA_PERMISSION_CODE = 100
    private val PROFILE_CAMERA_REQUEST_CODE = 101
    private val PROFILE_GALLERY_REQUEST_CODE = 102

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private lateinit var biography: String
    private lateinit var photoUrl: String

    private var cameraOptionsDialog: Dialog? = null
    private var newPhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserDetails()

        binding.cameraIconProfile.setOnClickListener {
            showPhotoOptionsDialog()
        }

        binding.cameraIconBanner.setOnClickListener {

        }

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
            return false
        }
        if (username.length > 20 || firstName.length > 40 || biography.length > 160) {
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
                        "biography" to biography,
                        "profileImage" to photoUrl
                    )

                    usersCollection.document(userId)
                        .update(user)
                        .addOnSuccessListener {
                            showAlertToast("User data updated successfully.")
                            savePhotoUrlToFirestore()

                            val resultIntent = Intent()
                            resultIntent.putExtra("username", username)
                            resultIntent.putExtra("firstName", firstName)
                            resultIntent.putExtra("biography", biography)
                            resultIntent.putExtra("photoUrl", newPhotoUrl)
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
            "biography" to biography,
            "profileImage" to photoUrl
        )

        usersCollection.document(userId)
            .update(user)
            .addOnSuccessListener {
                showAlertToast("User data updated successfully.")
                savePhotoUrlToFirestore()

                val resultIntent = Intent()
                resultIntent.putExtra("username", username)
                resultIntent.putExtra("firstName", firstName)
                resultIntent.putExtra("biography", biography)
                resultIntent.putExtra("photoUrl", newPhotoUrl)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener { e ->
                showAlertToast("Error updating user data: ${e.message}")
            }
    }

    private fun savePhotoUrlToFirestore() {
        newPhotoUrl?.let { url ->
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")
            val userUpdate = hashMapOf<String, Any>("profileImage" to url)
            usersCollection.document(userId)
                .update(userUpdate)
                .addOnSuccessListener {
                    showAlertToast("Profile photo updated successfully.")
                    newPhotoUrl = null
                }
                .addOnFailureListener { e ->
                    showAlertToast("Error updating profile photo URL: ${e.message}")
                }
        }
    }

    private fun showPhotoOptionsDialog() {
        if (cameraOptionsDialog == null) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.camera_options, null)
            val takePhotoButton = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val choosePhotoButton = dialogView.findViewById<Button>(R.id.btnChoosePhoto)

            takePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                } else {
                    openCamera()
                }
                cameraOptionsDialog?.dismiss()
            }

            choosePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PROFILE_GALLERY_REQUEST_CODE)
                } else {
                    openGallery()
                }
                cameraOptionsDialog?.dismiss()
            }

            cameraOptionsDialog = Dialog(this)
            cameraOptionsDialog?.setContentView(dialogView)
            cameraOptionsDialog?.setCancelable(true)
        }

        cameraOptionsDialog?.show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, PROFILE_CAMERA_REQUEST_CODE)
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PROFILE_GALLERY_REQUEST_CODE)
        } else {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PROFILE_GALLERY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PROFILE_CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // Aplicar máscara circular a la imagen
                    Glide.with(this)
                        .load(imageBitmap)
                        .transform(CircleCrop())
                        .into(binding.editUserCircle)

                    savePhotoToFirestore(imageBitmap)
                }
                PROFILE_GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    // Aplicar máscara circular a la imagen
                    Glide.with(this)
                        .load(imageBitmap)
                        .transform(CircleCrop())
                        .into(binding.editUserCircle)

                    savePhotoToFirestore(imageBitmap)
                }
            }
        }
    }

    private fun savePhotoToFirestore(bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child("/users/${userId}/profile/photo_profile.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                newPhotoUrl = uri.toString()
            }.addOnFailureListener {
                showAlertToast("Error getting photo URL: ${it.message}")
            }
        }.addOnFailureListener { exception ->
            showAlertToast("Error uploading photo: ${exception.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PROFILE_GALLERY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        cameraOptionsDialog?.dismiss()
    }
}
