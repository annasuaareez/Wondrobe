package com.example.wondrobe.ui.user.PostDetails

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.wondrobe.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class EditPost : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val postImage = findViewById<ImageView>(R.id.postImage)
        val newTitlePostEditText = findViewById<EditText>(R.id.newTitlePost)
        val newDescriptionPostEditText = findViewById<EditText>(R.id.newDescriptionPost)
        val datePost = findViewById<TextView>(R.id.newPostDate)
        val saveButton = findViewById<Button>(R.id.savePost)
        val deleteButton = findViewById<Button>(R.id.deleteAccountButton)

        val postId = intent.getStringExtra("PostID")
        val userId = intent.getStringExtra("UserID")
        val db = FirebaseFirestore.getInstance()

        if (postId != null) {
            // Obtener la información de la publicación de Firestore
            db.collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val title = document.getString("title")
                        val description = document.getString("description")
                        val imageUrl = document.getString("imageUrl")
                        val date = document.getString("date")

                        // Establecer la imagen en el ImageView
                        Glide.with(this)
                            .load(imageUrl)
                            .into(postImage)

                        if (!date.isNullOrEmpty()) {
                            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                            try {
                                val dateFormatted = dateFormat.parse(date)
                                // Formatear la fecha solo como "dd/MM/yyyy"
                                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateFormatted)
                                datePost.text = formattedDate
                            } catch (e: ParseException) {
                                Log.e("DetailsPost", "Error parsing date: ${e.message}")
                            }
                        }

                        // Establecer los hints de los EditText con la información de la publicación
                        newTitlePostEditText.hint = title
                        newDescriptionPostEditText.hint = description
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditPost", "Error getting post: ${e.message}")
                }
        }

        saveButton.setOnClickListener {
            val newTitle = newTitlePostEditText.text.toString()
            val newDescription = newDescriptionPostEditText.text.toString()

            val currentTitleHint = newTitlePostEditText.hint.toString()

            if (newTitle.isEmpty()) {
                // Si el nuevo título está vacío, se mantendrá el título que hay
                updatePostTitle(postId!!, currentTitleHint)
            } else {
                // Si el nuevo título no está vacío, se actualiza con el nuevo título
                updatePostTitle(postId!!, newTitle)
            }

            if (newDescription.isNotEmpty()) {
                // Si la descripción no está vacía, se actualiza
                updatePostDescription(postId, newDescription)
            }

            finish()
        }

        deleteButton.setOnClickListener {
            if (postId != null) {
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Confirm Delete Post")
                alertDialogBuilder.setMessage("Are you sure you want to delete this post?")
                alertDialogBuilder.setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    // Eliminar la cuenta y los datos asociados
                    deletePost(postId, userId)
                }
                alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    private fun updatePostTitle(postId: String, newTitle: String) {
        val db = FirebaseFirestore.getInstance()

        // Actualizar el título del post en Firestore
        db.collection("posts").document(postId)
            .update("title", newTitle)
            .addOnSuccessListener {
                // Lograr la actualización con éxito
                Log.i("EditPost", "Post title updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EditPost", "Error updating post title: ${e.message}")
            }
    }

    private fun updatePostDescription(postId: String, newDescription: String) {
        val db = FirebaseFirestore.getInstance()

        // Actualizar la descripción del post en Firestore
        db.collection("posts").document(postId)
            .update("description", newDescription)
            .addOnSuccessListener {
                // Lograr la actualización con éxito
                Log.i("EditPost", "Post description updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EditPost", "Error updating post description: ${e.message}")
            }
    }

    private fun deletePost(postId: String, userId: String?) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("/users/$userId/posts/post_$postId.jpg")

        // Eliminar el archivo de Storage
        storageRef.delete()
            .addOnSuccessListener {
                Log.i("EditPost", "Post image deleted from Storage successfully")
                deletePostFromFirestore(postId)
            }
            .addOnFailureListener { e ->
                // Si el archivo no existe, continúe eliminando el post
                if (e.message?.contains("Object does not exist") == true) {
                    Log.w("EditPost", "Post image does not exist in Storage, continuing deletion process")
                    deletePostFromFirestore(postId)
                } else {
                    Log.e("EditPost", "Error deleting post image from Storage: ${e.message}")
                    finish() // Error deleting image, finish the activity
                }
            }
    }

    private fun deletePostFromFirestore(postId: String) {
        val db = FirebaseFirestore.getInstance()

        // Eliminar el post de "posts"
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                Log.i("EditPost", "Post deleted from Firestore successfully")
                deletePostFromAllSavedPosts(postId)
            }
            .addOnFailureListener { e ->
                Log.e("EditPost", "Error deleting post from Firestore: ${e.message}")
                finish() // Error deleting post, finish the activity
            }
    }

    private fun deletePostFromAllSavedPosts(postId: String) {
        val db = FirebaseFirestore.getInstance()

        // Obtener todos los usuarios
        db.collection("users").get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val deleteTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

                    querySnapshot.documents.forEach { userDocument ->
                        val task = userDocument.reference.collection("savedPosts")
                            .whereEqualTo("postId", postId)
                            .get()
                            .continueWithTask { task ->
                                val savedPostsQuerySnapshot = task.result
                                if (savedPostsQuerySnapshot != null && !savedPostsQuerySnapshot.isEmpty) {
                                    val deleteSubTasks = savedPostsQuerySnapshot.documents.map { document ->
                                        document.reference.delete()
                                    }
                                    Tasks.whenAll(deleteSubTasks)
                                } else {
                                    Tasks.forResult(null) // Return a successful task if there's nothing to delete
                                }
                            }
                        deleteTasks.add(task)
                    }

                    // Wait for all deletions to complete
                    Tasks.whenAll(deleteTasks)
                        .addOnSuccessListener {
                            Log.i("EditPost", "All references to post deleted successfully")
                            finish() // All references deleted, finish the activity
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditPost", "Error deleting some references to post: ${e.message}")
                            finish() // Even if there's an error, finish the activity
                        }
                } else {
                    Log.i("EditPost", "No users found")
                    finish() // No users found, finish the activity
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditPost", "Error querying users: ${e.message}")
                finish() // Error querying users, finish the activity
            }
    }
}
