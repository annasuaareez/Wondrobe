package com.example.wondrobe.ui.user

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wondrobe.MainActivity
import com.example.wondrobe.R
import com.example.wondrobe.adapters.ImageFollowAdapter
import com.example.wondrobe.data.User
import com.example.wondrobe.utils.SharedPreferencesManager
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserFollow : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_follow)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        firestore = FirebaseFirestore.getInstance()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        delegate.applyDayNight()

        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        val selectedUser = intent.getParcelableExtra<User>("selected_user")
        val currentUserId = UserUtils.getUserId(this)

        Log.d("UserFollow", "Selected User: $selectedUser")
        Log.d("UserFollow", "Current User: $currentUserId")

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

            user.isFollowing = isFollowing

            updateFollowButton(followButton, user.isFollowing)

            followButton.setOnClickListener {
                user.isFollowing = !user.isFollowing
                SharedPreferencesManager.saveFollowingState(this, user.uid, user.isFollowing)

                updateFollowButton(followButton, user.isFollowing)

                if (user.isFollowing) {
                    updateFollowersCount(user, followersTextView, increment = true)
                    currentUserId?.let { updateFollowingCount(it, increment = true) }
                } else {
                    updateFollowersCount(user, followersTextView, increment = false)
                    currentUserId?.let { updateFollowingCount(it, increment = false) }
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

            selectedUser.uid?.let { userId ->
                val postsCollection = firestore.collection("posts")

                postsCollection.whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val imageUrls = mutableListOf<String>()
                        for (document in documents) {
                            val imageUrl = document.getString("imageUrl")
                            if (!imageUrl.isNullOrEmpty()) {
                                imageUrls.add(imageUrl)
                            }
                        }
                        if (imageUrls.isNotEmpty()) {
                            updatePostImages(imageUrls)
                        } else {
                            // No hay imagenes
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("loadUserPosts", "Error fetching user posts: ${e.message}")
                    }
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

    private fun updateFollowButton(button: Button, isFollowing: Boolean) {
        if (isFollowing) {
            button.text = "Following"
            button.setBackgroundResource(R.drawable.button_purple)
            button.setTextColor(Color.WHITE)
        } else {
            button.text = "Follow"
            button.setBackgroundResource(R.drawable.button_white)
            button.setTextColor(Color.BLACK)
        }
    }

    private fun updateFollowersCount(user: User, textView: TextView, increment: Boolean) {
        val userId = user.uid ?: return

        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(firestore.collection("users").document(userId))
            val currentFollowersCount = userDoc.getLong("followersCount") ?: 0L
            val newFollowersCount = if (increment) currentFollowersCount + 1 else maxOf(currentFollowersCount - 1, 0)

            transaction.update(firestore.collection("users").document(userId), "followersCount", newFollowersCount)
            newFollowersCount
        }.addOnSuccessListener { newFollowersCount ->
            SharedPreferencesManager.saveFollowersCount(this, user.uid, newFollowersCount.toInt())
            textView.text = newFollowersCount.toString()
            Log.d("Firestore", "Followers count updated successfully")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error updating followers count", e)
        }
    }

    private fun updateFollowingCount(currentUserId: String, increment: Boolean) {
        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(firestore.collection("users").document(currentUserId))
            val currentFollowingCount = userDoc.getLong("followingCount") ?: 0L
            val newFollowingCount = if (increment) currentFollowingCount + 1 else maxOf(currentFollowingCount - 1, 0)

            transaction.update(firestore.collection("users").document(currentUserId), "followingCount", newFollowingCount)
            newFollowingCount
        }.addOnSuccessListener { newFollowingCount ->
            SharedPreferencesManager.saveFollowingCount(this, currentUserId, newFollowingCount.toInt())
            Log.d("Firestore", "Following count updated successfully")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error updating following count", e)
        }
    }

    private fun updatePostImages(imageUrls: List<String>) {
        val recyclerView = findViewById<RecyclerView>(R.id.followView)
        val numColumns = 2
        val imageProfileAdapter = ImageFollowAdapter(this, imageUrls)
        val layoutManager = StaggeredGridLayoutManager(numColumns, StaggeredGridLayoutManager.VERTICAL)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = imageProfileAdapter
    }
}
