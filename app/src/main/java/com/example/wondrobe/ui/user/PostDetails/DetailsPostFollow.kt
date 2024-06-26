package com.example.wondrobe.ui.user.PostDetails

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.example.wondrobe.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class DetailsPostFollow : AppCompatActivity() {

    private lateinit var postId: String
    private lateinit var userId: String
    private var isPostSaved: Boolean = false
    private lateinit var saveIcon: ImageView
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_post_follow)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        postId = intent.getStringExtra("PostID") ?: ""
        userId = intent.getStringExtra("UserID") ?: ""

        Log.d("DetailsPostFollow", "Recivido PostID: $postId and UserID: $userId")

        Log.d("PostSelect", "Post Id: $postId")
        Log.d("UserSelect", "Current User Id: $userId")

        saveIcon = findViewById(R.id.saveIconPostFollowActivity)

        val db = FirebaseFirestore.getInstance()

        val backIcon = findViewById<ImageView>(R.id.backIconPostFollowActivity)
        val postImage = findViewById<ImageView>(R.id.postImageFollow)
        val postTitle = findViewById<TextView>(R.id.postTitleFollow)
        val postDescription = findViewById<TextView>(R.id.postDescriptionFollow)
        val postDate = findViewById<TextView>(R.id.postDateFollow)
        val postUsername = findViewById<TextView>(R.id.PostusernameFollow)

        backIcon.setOnClickListener {
            navigateToSelectPost()
        }

        loadPostSavedStateFromDatabase()

        saveIcon.setOnClickListener {
            toggleSavePost()
        }

        if (postId.isNotEmpty()) {
            Log.e("DetailsPost", "Post ID recibido: $postId, $userId")
            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val ownerId = document.getString("userId")
                        val title = document.getString("title")
                        val description = document.getString("description")
                        imageUrl = document.getString("imageUrl")
                        val date = document.getString("date")

                        updateSaveIconColor()

                        postTitle.text = title
                        postDescription.text = description

                        if (!date.isNullOrEmpty()) {
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                            try {
                                val dateFormatted = dateFormat.parse(date)
                                // Formatear la fecha solo como "dd/MM/yyyy"
                                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateFormatted)
                                postDate.text = formattedDate
                            } catch (e: ParseException) {
                                Log.e("DetailsPost", "Error parsing date: ${e.message}")
                            }
                        }

                        if (ownerId != null) {
                            db.collection("users").document(ownerId).get()
                                .addOnSuccessListener { userDocument ->
                                    if (userDocument != null && userDocument.exists()) {
                                        val username = userDocument.getString("username")
                                        postUsername.text = username
                                    } else {
                                        Log.e("DetailsPost", "User document not found for userId: $ownerId")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DetailsPost", "Error fetching user document: ${e.message}")
                                }
                        } else {
                            Log.e("DetailsPost", "OwnerId not found in post document")
                        }

                        if (!imageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(imageUrl)
                                .transform(FitCenter())
                                .into(postImage)
                        }

                        if (description.isNullOrEmpty()) {
                            // Si no hay descripción, ajustar las restricciones para que postDate esté debajo de titleLayout
                            val paramsDate = postDate.layoutParams as ConstraintLayout.LayoutParams
                            paramsDate.topToBottom = postTitle.id
                            postDate.layoutParams = paramsDate

                            // Actualizar el layout
                            postDate.requestLayout()
                        } else {
                            // Si hay descripción, ajustar las restricciones para que postDate esté debajo de postDescription
                            val paramsDate = postDate.layoutParams as ConstraintLayout.LayoutParams
                            paramsDate.topToBottom = postDescription.id
                            postDate.layoutParams = paramsDate

                            // Actualizar el layout
                            postDate.requestLayout()
                        }
                    } else {
                        Log.e("DetailsPost", "Post document not found for postId: $postId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetailsPost", "Error fetching post document: ${e.message}")
                }
        } else {
            Log.e("DetailsPost", "Post ID no encontrado en el Intent")
        }
    }

    private fun toggleSavePost() {
        Log.d("DetailsPost", "Toggling save state for postId: $postId, userId: $userId")
        Log.d("DetailsPost", "Current save state: $isPostSaved")

        isPostSaved = !isPostSaved
        updateSaveIconColor()

        val db = FirebaseFirestore.getInstance()
        val userSavesRef = db.collection("users").document(userId).collection("savedPosts").document(postId)

        if (isPostSaved) {
            val saveData = hashMapOf(
                "postId" to postId,
                "imageUrl" to imageUrl,
                "timestamp" to System.currentTimeMillis()
            )
            userSavesRef.set(saveData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("DetailsPost", "Post saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("DetailsPost", "Error saving post: ${e.message}")
                }
        } else {
            userSavesRef.delete()
                .addOnSuccessListener {
                    Log.d("DetailsPost", "Post unsaved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("DetailsPost", "Error unsaving post: ${e.message}")
                }
        }
    }

    private fun updateSaveIconColor() {
        val iconResource = if (isPostSaved) R.drawable.ic_save else R.drawable.ic_unsave
        saveIcon.setImageResource(iconResource)
    }

    private fun loadPostSavedStateFromDatabase() {
        val db = FirebaseFirestore.getInstance()
        val userSavesRef = db.collection("users").document(userId).collection("savedPosts").document(postId)

        userSavesRef.get()
            .addOnSuccessListener { document ->
                isPostSaved = document.exists()
                updateSaveIconColor()
            }
            .addOnFailureListener { e ->
                Log.e("DetailsPost", "Error fetching saved state: ${e.message}")
            }
    }

    private fun navigateToSelectPost() {
        finish()
    }
}
