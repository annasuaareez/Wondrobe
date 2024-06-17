package com.example.wondrobe.ui.add.clothes

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.MultiAutoCompleteTextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.R
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddClothes : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var imageUriString: String
    private lateinit var publishButton: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private lateinit var multiAutoComplete: MultiAutoCompleteTextView
    private lateinit var selectedOptions: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_clothes)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val userId = UserUtils.getUserId(this)
        val backIcon = findViewById<ImageView>(R.id.backIconClothesActivity)

        titleEditText = findViewById(R.id.titleClothes)
        imageView = findViewById(R.id.imageViewClothes)
        publishButton = findViewById(R.id.publishClothesButton)
        progressBar = findViewById(R.id.progressBarAddClothes)
        multiAutoComplete = findViewById(R.id.multiAutoComplete)
        selectedOptions = mutableListOf()

        imageUriString = intent.getStringExtra("imageUri") ?: ""

        val options = arrayOf("T-shirts", "Sweaters", "Blouses", "Jeans", "Leggings", "Pants", "Shorts", "Dress", "Jackets", "Coats",
            "Scarves", "Hats", "Jewelry", "Handbag", "Belts", "Sneakers", "Boots", "Sandals")

        // Usar ArrayAdapter en lugar de ClothesAdapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, options)
        multiAutoComplete.setAdapter(adapter)
        multiAutoComplete.setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())

        backIcon.setOnClickListener {
            navigateToSelectClothes()
        }

        if (imageUriString.isNotEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            try {
                imageView.setImageURI(imageUri)
            } catch (e: Exception) {
                Log.e("AddClothes", "Error setting image URI: ${e.message}")
                showAlertToast("Error loading image")
            }
        }

        publishButton.setOnClickListener {
            if (userId == null) {
                Log.e("User not Authentic", "User not Authentic")
            } else {
                Log.e("AddClothes", userId)
                validateAndPublishClothes(userId, options)
            }
        }

        // Mostrar el diálogo emergente del MultiAutoCompleteTextView
        multiAutoComplete.setOnClickListener {
            showOptionsDialog(options)
        }
    }

    private fun validateAndPublishClothes(userId: String, options: Array<String>) {
        val title = titleEditText.text.toString().trim()
        val typeClothes = multiAutoComplete.text.toString().trim()

        if (title.isEmpty()) {
            showAlertToast("Insert a title")
            return
        }

        if (typeClothes.isEmpty()) {
            showAlertToast("Select a type of clothes")
            return
        }

        if (!options.contains(typeClothes)) {
            showAlertToast("Invalid type of clothes selected")
            return
        }

        // If validation is successful, disable the button and show the progress bar
        publishButton.isEnabled = false
        publishButton.setBackgroundResource(R.drawable.button_gray)
        progressBar.visibility = View.VISIBLE

        publishClothes(userId, title, typeClothes)
    }

    private fun publishClothes(userId: String, title: String, typeClothes: String) {
        val firestore = FirebaseFirestore.getInstance()
        val clothesCollection = firestore.collection("clothes")

        val currentDate = Calendar.getInstance().time
        val formattedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(currentDate)

        val clothes = hashMapOf(
            "userId" to userId,
            "imageUrl" to imageUriString,
            "title" to title,
            "typeClothes" to typeClothes,
            "date" to formattedDate
        )

        clothesCollection.add(clothes)
            .addOnSuccessListener { documentReference ->
                val clotheId = documentReference.id
                saveImageToFirebaseStorage(imageUriString, clotheId, typeClothes) { imageUrl ->
                    if (imageUrl != null) {
                        clothesCollection.document(clotheId)
                            .update("imageUrl", imageUrl)
                            .addOnSuccessListener {
                                showAlertToast("Clothes published successfully")
                                navigateToSelectClothes()
                            }
                            .addOnFailureListener { e ->
                                showAlertToast("Error saving image URL: ${e.message}")
                                resetPublishButton()
                            }
                            .addOnCompleteListener {
                                progressBar.visibility = View.INVISIBLE
                            }
                    } else {
                        showAlertToast("Error saving image")
                        resetPublishButton()
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error publishing post: ${e.message}")
                resetPublishButton()
                progressBar.visibility = View.INVISIBLE
            }
    }

    private fun saveImageToFirebaseStorage(imageUri: String, clothe: String, selectedType: String, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val clotheId = UserUtils.getUserId(this)
        val imagesRef = storageRef.child("/users/$clotheId/clothes/$selectedType/clothe_$clothe.jpg")

        try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(imageUri))
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = imagesRef.putBytes(data)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                }.addOnFailureListener {
                    callback(null)
                }
            }.addOnFailureListener { exception ->
                callback(null)
            }
        } catch (e: Exception) {
            Log.e("AddClothes", "Error saving image to Firebase Storage: ${e.message}")
            callback(null)
        }
    }

    private fun navigateToSelectClothes() {
        val intent = Intent(this, SelectClothes::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showOptionsDialog(options: Array<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Options")
        builder.setMultiChoiceItems(options, null) { _, index, isChecked ->
            if (isChecked) {
                selectedOptions.add(options[index])
            } else {
                selectedOptions.remove(options[index])
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            multiAutoComplete.setText(selectedOptions.joinToString(", "))
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun resetPublishButton() {
        publishButton.isEnabled = true
        publishButton.setBackgroundResource(R.drawable.button_purple)
    }
}
