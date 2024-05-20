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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_post_follow)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        postId = intent.getStringExtra("PostID") ?: ""
        userId = intent.getStringExtra("UserID") ?: ""

        Log.d("PostSelect", "Post Id: $postId")
        Log.e("UserSelect", "Current User Id: $userId")

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

        if (postId != null) {
            Log.e("DetailsPost", "Post ID recibido: $postId, $userId")
            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userId = document.getString("userId")
                        val title = document.getString("title")
                        val description = document.getString("description")
                        val imageUrl = document.getString("imageUrl")
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

                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { userDocument ->
                                    if (userDocument != null && userDocument.exists()) {
                                        val username = userDocument.getString("username")
                                        postUsername.text = username
                                    } else {
                                        Log.e("DetailsPost", "User document not found for userId: $userId")
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DetailsPost", "Error fetching user document: ${e.message}")
                                }
                        } else {
                            Log.e("DetailsPost", "UserId not found in post document")
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
        isPostSaved = !isPostSaved
        updateSaveIconColor()

        val db = FirebaseFirestore.getInstance()
        val saveData = hashMapOf("saved" to isPostSaved)

        // Guardar el estado actualizado del post en la colección "posts"
        db.collection("posts").document(postId)
            .set(saveData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("DetailsPost", "Post saved state updated successfully")

                // Obtener el ID de usuario del documento del post
                db.collection("posts").document(postId).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val postOwnerId = document.getString("userId")
                            val imageUrl = document.getString("imageUrl")

                            if (postOwnerId != null && imageUrl != null) {
                                // Si el post está guardado, agregarlo a la colección "postSave"
                                if (isPostSaved) {
                                    val savePostData = hashMapOf(
                                        "postId" to postId,
                                        "loggedInUserId" to userId,
                                        "postOwnerId" to postOwnerId,
                                        "imageUrl" to imageUrl
                                    )

                                    db.collection("postSave").add(savePostData)
                                        .addOnSuccessListener {
                                            Log.d("DetailsPost", "Post saved in the postSave collection")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DetailsPost", "Error saving post in the postSave collection: ${e.message}")
                                        }
                                } else {
                                    // Si el post no está guardado, eliminarlo de "postSave" si existe
                                    db.collection("postSave")
                                        .whereEqualTo("postId", postId)
                                        .whereEqualTo("loggedInUserId", userId)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            for (document in documents) {
                                                document.reference.delete()
                                                    .addOnSuccessListener {
                                                        Log.d("DetailsPost", "Post removed from the postSave collection")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("DetailsPost", "Error removing post from the postSave collection: ${e.message}")
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("DetailsPost", "Error querying the postSave collection: ${e.message}")
                                        }
                                }
                            } else {
                                Log.e("DetailsPost", "User ID not found in the post document")
                            }
                        } else {
                            Log.e("DetailsPost", "Post document not found for postId: $postId")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DetailsPost", "Error fetching post document: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DetailsPost", "Error updating post saved state: ${e.message}")
            }
    }

    private fun updateSaveIconColor() {
        val iconResource = if (isPostSaved) R.drawable.ic_save else R.drawable.ic_unsave
        saveIcon.setImageResource(iconResource)
    }

    private fun loadPostSavedStateFromDatabase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("posts").document(postId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    isPostSaved = document.getBoolean("saved") ?: false
                    updateSaveIconColor()
                } else {
                    Log.e("DetailsPost", "Post document not found for postId: $postId")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailsPost", "Error fetching post document: ${e.message}")
            }
    }

    private fun navigateToSelectPost() {
        finish()
    }
}