package com.example.wondrobe.ui.add.post

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.R
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPost : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var imageUriString: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        val userId = UserUtils.getUserId(this)
        val backIcon = findViewById<ImageView>(R.id.backIconActivity)
        val publishButton = findViewById<AppCompatButton>(R.id.publishButton)

        titleEditText = findViewById(R.id.titlePost)
        descriptionEditText = findViewById(R.id.descriptionPost)
        imageView = findViewById(R.id.imageViewPost)

        imageUriString = intent.getStringExtra("imageUri") ?: ""

        backIcon.setOnClickListener {
            navigateToSelectPost()
        }

        if (imageUriString.isNotEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            try {
                imageView.setImageURI(imageUri)
            } catch (e: Exception) {
                Log.e("AddPost", "Error setting image URI: ${e.message}")
                showAlertToast("Error loading image")
            }
        }

        publishButton.setOnClickListener {
            if (userId == null) {
                Log.e("User not Authentic", "User not Authentic")
            } else {
                Log.e("AddPost", userId)
                publishPost(userId)
            }
        }
    }

    private fun publishPost(userId: String) {
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (title.isEmpty()) {
            showAlertToast("Insert a title")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val postsCollection = firestore.collection("posts")

        val currentDate = Calendar.getInstance().time
        val formattedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(currentDate)

        val post = hashMapOf(
            "userId" to userId,
            "imageUrl" to imageUriString,
            "title" to title,
            "description" to description,
            "date" to formattedDate
        )

        postsCollection.add(post)
            .addOnSuccessListener { documentReference ->
                val postId = documentReference.id
                saveImageToFirebaseStorage(imageUriString, postId) { imageUrl ->
                    if (imageUrl != null) {
                        // Save image URL to Firestore
                        postsCollection.document(postId)
                            .update("imageUrl", imageUrl)
                            .addOnSuccessListener {
                                showAlertToast("Post published successfully")
                                finish()
                            }
                            .addOnFailureListener { e ->
                                showAlertToast("Error saving image URL: ${e.message}")
                            }
                    } else {
                        showAlertToast("Error saving image")
                    }
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error publishing post: ${e.message}")
            }
    }

    private fun saveImageToFirebaseStorage(imageUri: String, postId: String, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = UserUtils.getUserId(this)
        val imagesRef = storageRef.child("/users/$userId/posts/post_$postId.jpg")

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
            Log.e("AddPost", "Error saving image to Firebase Storage: ${e.message}")
            callback(null)
        }
    }

    private fun navigateToSelectPost() {
        val intent = Intent(this, SelectPost::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
