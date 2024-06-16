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
import com.example.wondrobe.ui.user.PostDetails.DetailsPostFollow
import com.example.wondrobe.utils.SharedPreferencesManager
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserFollow : AppCompatActivity(), ImageFollowAdapter.OnImageClickListener {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var followButton: Button
    private lateinit var followersTextView: TextView

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
        val followingTextView = findViewById<TextView>(R.id.followingCount)
        val arrowIcon = findViewById<ImageView>(R.id.arrowIcon)
        val followingLayout = findViewById<LinearLayout>(R.id.followingLayoutUser)
        val followersLayout = findViewById<LinearLayout>(R.id.followersLayoutUser)

        followButton = findViewById(R.id.followButton)
        followersTextView = findViewById(R.id.followersCount)

        selectedUser?.let { user ->
            usernameTextView.text = user.username
            firstNameTextView.text = user.firstName
            biographyTextView.text = user.biography

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

            user.uid?.let { userId ->
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val followersCount = document.getLong("followersCount")?.toInt() ?: 0
                            val followingCount = document.getLong("followingCount")?.toInt() ?: 0

                            SharedPreferencesManager.saveFollowersCount(this, userId, followersCount)
                            SharedPreferencesManager.saveFollowingCount(this, userId, followingCount)
                            followersTextView.text = followersCount.toString()
                            followingTextView.text = followingCount.toString()

                            val userFollowsRef = firestore.collection("users").document(currentUserId!!)
                                .collection("userFollow").document(userId)

                            userFollowsRef.get().addOnSuccessListener { doc ->
                                user.isFollowing = doc.exists()
                                updateFollowButton(followButton, user.isFollowing)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserFollow", "Error fetching user data: ${e.message}")
                    }

                val postsCollection = firestore.collection("posts")
                postsCollection.whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val imageUrls = mutableListOf<String>()
                        val postIds = mutableListOf<String>()
                        for (document in documents) {
                            val imageUrl = document.getString("imageUrl")
                            val postId = document.id
                            if (!imageUrl.isNullOrEmpty()) {
                                imageUrls.add(imageUrl)
                                postIds.add(postId)
                            }
                        }
                        if (imageUrls.isNotEmpty()) {
                            updatePostImages(imageUrls, postIds)
                        } else {
                            // No hay imagenes
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("loadUserPosts", "Error fetching user posts: ${e.message}")
                    }
            }

            followButton.setOnClickListener {
                toggleFollowUser(user)
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

    private fun toggleFollowUser(selectedUser: User) {
        val userId = selectedUser.uid ?: return
        val currentUserId = UserUtils.getUserId(this) ?: return

        val isFollowing = !SharedPreferencesManager.getFollowingState(this, userId)
        SharedPreferencesManager.saveFollowingState(this, userId, isFollowing)

        selectedUser.isFollowing = isFollowing
        updateFollowButton(followButton, isFollowing)

        val db = FirebaseFirestore.getInstance()
        val userFollowsRef = db.collection("users").document(userId)
            .collection("userFollowers").document(currentUserId)

        if (isFollowing) {
            val followerData = hashMapOf("followerId" to currentUserId)
            userFollowsRef.set(followerData)
                .addOnSuccessListener {
                    Log.d("UserFollow", "User followed successfully")
                    updateFollowersCount(selectedUser, followersTextView, true, currentUserId)
                    updateFollowingCount(currentUserId, true)
                }
                .addOnFailureListener { e ->
                    Log.e("UserFollow", "Error following user: ${e.message}")
                }

            val currentUserData = hashMapOf("followedUserId" to userId)
            db.collection("users").document(currentUserId)
                .collection("userFollow").document(userId)
                .set(currentUserData)
                .addOnSuccessListener {
                    Log.d("UserFollow", "Current user added to userFollow successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("UserFollow", "Error adding current user to userFollow: ${e.message}")
                }
        } else {
            userFollowsRef.delete()
                .addOnSuccessListener {
                    Log.d("UserFollow", "User unfollowed successfully")
                    updateFollowersCount(selectedUser, followersTextView, false, currentUserId)
                    updateFollowingCount(currentUserId, false)
                }
                .addOnFailureListener { e ->
                    Log.e("UserFollow", "Error unfollowing user: ${e.message}")
                }

            db.collection("users").document(currentUserId)
                .collection("userFollow").document(userId)
                .delete()
                .addOnSuccessListener {
                    Log.d("UserFollow", "Current user removed from userFollow successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("UserFollow", "Error removing current user from userFollow: ${e.message}")
                }
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

    private fun updateFollowersCount(user: User, textView: TextView, increment: Boolean, currentUserId: String) {
        val userId = user.uid ?: return
        val followersCount = if (increment) {
            SharedPreferencesManager.getFollowersCount(this, userId) + 1
        } else {
            SharedPreferencesManager.getFollowersCount(this, userId) - 1
        }
        SharedPreferencesManager.saveFollowersCount(this, userId, followersCount)
        textView.text = followersCount.toString()

        firestore.collection("users").document(userId)
            .update("followersCount", followersCount)
            .addOnSuccessListener {
                Log.d("UserFollow", "Followers count updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("UserFollow", "Error updating followers count in Firestore: ${e.message}")
            }
    }

    private fun updateFollowingCount(currentUserId: String, increment: Boolean) {
        val followingCount = if (increment) {
            SharedPreferencesManager.getFollowingCount(this, currentUserId) + 1
        } else {
            SharedPreferencesManager.getFollowingCount(this, currentUserId) - 1
        }
        SharedPreferencesManager.saveFollowingCount(this, currentUserId, followingCount)

        firestore.collection("users").document(currentUserId)
            .update("followingCount", followingCount)
            .addOnSuccessListener {
                Log.d("UserFollow", "Following count updated in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("UserFollow", "Error updating following count in Firestore: ${e.message}")
            }
    }

    private fun updatePostImages(imageUrls: List<String>, postIds: List<String>) {
        val recyclerView: RecyclerView = findViewById(R.id.followView)
        val adapter = ImageFollowAdapter(this, imageUrls, postIds, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    override fun onImageClick(postId: String) {
        val currentUserId = UserUtils.getUserId(this)
        val intent = Intent(this, DetailsPostFollow::class.java)
        intent.putExtra("PostID", postId)
        intent.putExtra("UserID", currentUserId)
        startActivity(intent)
    }
}
