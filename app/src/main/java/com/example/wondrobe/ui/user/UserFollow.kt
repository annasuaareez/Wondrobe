package com.example.wondrobe.ui.user

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wondrobe.MainActivity
import com.example.wondrobe.R
import com.example.wondrobe.data.User
import com.example.wondrobe.utils.SharedPreferencesManager
import com.google.firebase.firestore.FirebaseFirestore

class UserFollow : AppCompatActivity() {
    // Declare Firestore reference
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_follow)

        // Initialize Firestore reference
        firestore = FirebaseFirestore.getInstance()

        // Listen for changes in the app's theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        delegate.applyDayNight()

        // Get the current night mode
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        // Get information of the selected user from the Intent
        val selectedUser = intent.getParcelableExtra<User>("selected_user")

        Log.d("UserFollow", "Selected User: $selectedUser")

        // Configure views with user information
        val imageView = findViewById<ImageView>(R.id.profileImage)
        val usernameTextView = findViewById<TextView>(R.id.username)
        val firstNameTextView = findViewById<TextView>(R.id.firstName)
        val biographyTextView = findViewById<TextView>(R.id.biography)
        val bannerImageView = findViewById<ImageView>(R.id.banner)
        val followersTextView = findViewById<TextView>(R.id.followersCount)
        val followingTextView = findViewById<TextView>(R.id.followingCount)
        val arrowIcon = findViewById<ImageView>(R.id.arrowIcon)
        val followButton = findViewById<Button>(R.id.followButton)
        val followingLayout = findViewById<LinearLayout>(R.id.followingLayoutUser)
        val followersLayout = findViewById<LinearLayout>(R.id.followersLayoutUser)

        selectedUser?.let { user ->
            usernameTextView.text = user.username
            firstNameTextView.text = user.firstName
            biographyTextView.text = user.biography
            followersTextView.text = user.followersCount.toString()
            followingTextView.text = user.followingCount.toString()

            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_user)
                    .transform(CircleCrop())
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_user)
            }

            if (!user.bannerImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.bannerImage)
                    .into(bannerImageView)
            } else {
                bannerImageView.setColorFilter(ContextCompat.getColor(this, R.color.light_blue_gray))
            }

            val isFollowing = SharedPreferencesManager.getFollowingState(this, user.uid)
            val followersCount = SharedPreferencesManager.getFollowersCount(this, user.uid)

            user.isFollowing = isFollowing

            if (user.isFollowing) {
                followButton.text = "Following"
                followButton.setBackgroundResource(R.drawable.button_purple)
                followButton.setTextColor(Color.WHITE)
            } else {
                followButton.text = "Follow"
                followButton.setBackgroundResource(R.drawable.button_white)
            }

            followButton.setOnClickListener {
                user.isFollowing = !user.isFollowing
                SharedPreferencesManager.saveFollowingState(this, user.uid, user.isFollowing)

                if (user.isFollowing) {
                    followButton.text = "Following"
                    followButton.setBackgroundResource(R.drawable.button_purple)
                    followButton.setTextColor(Color.WHITE)

                    // Incrementar el contador de seguidores del usuario seleccionado
                    val updatedFollowersCount = followersCount + 1
                    SharedPreferencesManager.saveFollowersCount(this, user.uid, updatedFollowersCount)
                    followersTextView.text = updatedFollowersCount.toString()

                    // Actualizar el contador de seguidores en Firestore
                    user.uid?.let { userId ->
                        firestore.collection("users").document(userId)
                            .update("followersCount", updatedFollowersCount)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Followers count updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating followers count", e)
                            }
                    }

                    val currentUser = SharedPreferencesManager.getCurrentUser(this)
                    Log.d("Follow", "User ${currentUser?.username} followed ${user.username}")
                } else {
                    followButton.text = "Follow"
                    followButton.setBackgroundResource(R.drawable.button_white)
                    followButton.setTextColor(Color.BLACK)

                    // Decrementar el contador de seguidores del usuario seleccionado
                    val updatedFollowersCount = followersCount - 1
                    SharedPreferencesManager.saveFollowersCount(this, user.uid, updatedFollowersCount)
                    followersTextView.text = updatedFollowersCount.toString()

                    // Actualizar el contador de seguidores en Firestore
                    user.uid?.let { userId ->
                        firestore.collection("users").document(userId)
                            .update("followersCount", updatedFollowersCount)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Followers count updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating followers count", e)
                            }
                    }

                    val currentUser = SharedPreferencesManager.getCurrentUser(this)
                    Log.d("Follow", "User ${currentUser?.username} followed ${user.username}")
                }
            }

            if (user.biography.isNullOrEmpty()) {
                val paramsFollowing = followingLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowing.topToBottom = firstNameTextView.id
                followingLayout.layoutParams = paramsFollowing

                val paramsFollowers = followersLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowers.topToBottom = firstNameTextView.id
                followersLayout.layoutParams = paramsFollowers
            } else {
                val paramsFollowing = followingLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowing.topToBottom = biographyTextView.id
                followingLayout.layoutParams = paramsFollowing

                val paramsFollowers = followersLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowers.topToBottom = biographyTextView.id
                followersLayout.layoutParams = paramsFollowers
            }
        }

        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            arrowIcon.setImageResource(R.drawable.ic_back_white)
        } else {
            arrowIcon.setImageResource(R.drawable.ic_back)
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            startActivity(intent)
            finish()
        }
    }
}