package com.example.wondrobe.ui.home

import PostAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wondrobe.R
import com.example.wondrobe.adapters.UserAdapter
import com.example.wondrobe.data.Post
import com.example.wondrobe.data.User
import com.example.wondrobe.databinding.FragmentHomeBinding
import com.example.wondrobe.ui.user.PostDetails.DetailsPostFollow
import com.example.wondrobe.ui.user.UserFollow
import com.example.wondrobe.utils.SaveManager
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserUid: String
    private lateinit var searchView: SearchView
    private lateinit var listView: ListView
    private lateinit var blackOverlay: View
    private lateinit var optionsLayout: LinearLayout
    private lateinit var recyclerViewFollowingUsers: RecyclerView
    private lateinit var recyclerViewAllPosts: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var allPostsAdapter: PostAdapter

    companion object {
        private const val REQUEST_USER_FOLLOW = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        currentUserUid = auth.currentUser?.uid ?: ""

        val userId = UserUtils.getUserId(requireContext()).toString()

        Log.e("HomeFragment", "UID del usuario: $userId")

        val forYouButton = binding.forYouButton
        val followingButton = binding.followingButton
        val forYouIndicator = binding.forYouIndicator
        val followingIndicator = binding.followingIndicator

        searchView = binding.searchView
        listView = binding.listUsers
        blackOverlay = binding.blackOverlay
        optionsLayout = binding.optionsLayout
        recyclerViewFollowingUsers = binding.recyclerViewFollowingUsers
        recyclerViewFollowingUsers.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewAllPosts = binding.recyclerViewAllPosts
        recyclerViewAllPosts.layoutManager = LinearLayoutManager(requireContext())

        loadAllPosts()

        postAdapter = PostAdapter(requireContext(), emptyList(), object : PostAdapter.OnPostClickListener {
            override fun onSaveClick(post: Post, saveIconView: ImageView) {
                toggleSavePost(post, saveIconView)
            }
        }, this::isPostSaved)
        recyclerViewFollowingUsers.adapter = postAdapter

        allPostsAdapter = PostAdapter(requireContext(), emptyList(), object : PostAdapter.OnPostClickListener {
            /*override fun onPostClick(post: Post) {
                val intent = Intent(requireContext(), DetailsPostFollow::class.java)
                intent.putExtra("PostID", post.postId)
                intent.putExtra("UserID", post.userId)
                startActivity(intent)
            }*/

            override fun onSaveClick(post: Post, saveIconView: ImageView) {
                toggleSavePost(post, saveIconView)
            }
        }, this::isPostSaved)
        recyclerViewAllPosts.adapter = allPostsAdapter

        listView.visibility = View.GONE  // Ocultar el ListView inicialmente

        searchView.setOnSearchClickListener {
            blackOverlay.visibility = View.VISIBLE
            listView.visibility = View.VISIBLE  // Mostrar el ListView cuando la búsqueda está activa
            optionsLayout.elevation = 4f
        }

        searchView.setOnCloseListener {
            blackOverlay.visibility = View.GONE
            listView.visibility = View.GONE  // Ocultar el ListView cuando la búsqueda no está activa
            updateListView(emptyList(), listView)
            optionsLayout.elevation = 12f
            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isNotBlank()) {
                        if (it != userId && it != auth.currentUser?.displayName) {
                            searchUsers(it, listView)
                            return true
                        } else {
                            updateListView(emptyList(), listView)
                            searchView.setQuery("", false)
                            return true
                        }
                    } else {
                        updateListView(emptyList(), listView) // Actualiza la lista con una lista vacía
                        return true
                    }
                }
                return false
            }
        })

        forYouButton.setOnClickListener {
            animateIndicatorChange(forYouIndicator, followingIndicator)
            recyclerViewFollowingUsers.visibility = View.GONE
            recyclerViewAllPosts.visibility = View.VISIBLE
            loadAllPosts()
        }

        followingButton.setOnClickListener {
            animateIndicatorChange(followingIndicator, forYouIndicator)
            recyclerViewFollowingUsers.visibility = View.VISIBLE
            recyclerViewAllPosts.visibility = View.GONE
            loadFollowingPosts()
        }

        return root
    }

    private fun searchUsers(query: String, listView: ListView) {
        val db = FirebaseFirestore.getInstance()
        val userId = UserUtils.getUserId(requireContext()).toString()

        db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThan("username", query + "\uf8ff")
            .limit(8)
            .get()
            .addOnSuccessListener { documents ->
                val usersList = mutableListOf<User>()
                for (document in documents) {
                    val uid = document.id
                    if (uid != userId) {
                        val email = document.getString("email") ?: ""
                        val username = document.getString("username") ?: ""
                        val firstName = document.getString("firstName") ?: ""
                        val biography = document.getString("biography") ?: ""
                        val profileImage = document.getString("profileImage") ?: ""
                        val bannerImage = document.getString("bannerImage") ?: ""
                        val followersCount = document.getLong("followersCount")?.toInt() ?: 0
                        val followingCount = document.getLong("followingCount")?.toInt() ?: 0

                        val user = User(uid, email, username, firstName, biography, "", profileImage, bannerImage,
                            false, followersCount, followingCount)
                        usersList.add(user)
                    }
                }
                updateListView(usersList, listView)
            }
            .addOnFailureListener { exception ->
                // Manejar errores
                Log.e("HomeFragment", "Error searching users", exception)
            }
    }

    private fun updateListView(usersList: List<User>, listView: ListView) {
        Log.d("HomeFragment", "Number of users: ${usersList.size}")
        usersList.forEachIndexed { index, user ->
            Log.d("HomeFragment", "User $index: $user")
        }

        val adapter = UserAdapter(requireContext(), usersList)
        listView.adapter = adapter

        if (usersList.isEmpty()) {
            listView.visibility = View.GONE
            optionsLayout.elevation = 12f // Aumentar la elevación cuando el ListView está vacío
        } else {
            listView.visibility = View.VISIBLE
            optionsLayout.elevation = 4f // Reducir la elevación cuando el ListView tiene elementos
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val userId = UserUtils.getUserId(requireContext()).toString()
            val selectedUser = usersList[position]
            val intent = Intent(requireContext(), UserFollow::class.java)
            intent.putExtra("selected_user", selectedUser)
            intent.putExtra("UserID", userId)
            startActivityForResult(intent, REQUEST_USER_FOLLOW)
        }
    }

    private fun animateIndicatorChange(showIndicator: View, hideIndicator: View) {
        val hideAnimator = ObjectAnimator.ofFloat(hideIndicator, "alpha", 1f, 0f)
        hideAnimator.duration = 250 // Duración de la animación en milisegundos
        hideAnimator.interpolator = AccelerateDecelerateInterpolator()

        val showAnimator = ObjectAnimator.ofFloat(showIndicator, "alpha", 0f, 1f)
        showAnimator.duration = 250 // Duración de la animación en milisegundos
        showAnimator.interpolator = AccelerateDecelerateInterpolator()

        hideAnimator.addUpdateListener {
            if (hideIndicator.alpha == 0f) {
                hideIndicator.visibility = View.INVISIBLE
            }
        }

        showAnimator.addUpdateListener {
            if (showIndicator.alpha == 1f) {
                showIndicator.visibility = View.VISIBLE
            }
        }

        hideAnimator.start()
        showAnimator.start()
    }

    private fun loadAllPosts() {
        val currentUserUid = UserUtils.getUserId(requireContext()).toString()

        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val usersIds = documents.mapNotNull { it.id }.filter { it != currentUserUid }

                if (usersIds.isNotEmpty()) {
                    db.collection("posts")
                        .whereIn("userId", usersIds)
                        .get()
                        .addOnSuccessListener { documents ->
                            val posts = mutableListOf<Post>()
                            for (document in documents) {
                                val postId = document.id
                                val userId = document.getString("userId") ?: ""
                                val imageUrl = document.getString("imageUrl") ?: ""
                                val title = document.getString("title") ?: ""
                                val description = document.getString("description") ?: ""
                                val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()

                                val post = Post(postId, userId, imageUrl, title, description, timestamp)
                                posts.add(post)
                            }
                            updatePostSaveStates(posts)
                            allPostsAdapter.updateData(posts)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HomeFragment", "Error getting posts", exception)
                        }
                } else {
                    // Handle the case when usersIds is empty
                    Log.d("HomeFragment", "No users found other than the current user.")
                    // Optionally update the UI to reflect no posts found
                    allPostsAdapter.updateData(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error getting users", exception)
            }
    }

    private fun loadFollowingPosts() {
        //val currentUserUid = auth.currentUser?.uid ?:
        val currentUserUid = UserUtils.getUserId(requireContext()).toString()
        Log.d("HomeFragment", "Cargando posts de usuarios seguidos para UID: $currentUserUid")

        // Obtener los IDs de los usuarios seguidos
        db.collection("users").document(currentUserUid).collection("userFollow")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("HomeFragment", "No se encontraron documentos en userFollowers")
                }
                val followedUsersIds = documents.mapNotNull { it.getString("followedUserId") }
                Log.d("HomeFragment", "Usuarios seguidos: $followedUsersIds")
                fetchPosts(followedUsersIds)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error obteniendo usuarios seguidos", exception)
            }
    }

    private fun fetchPosts(followedUsersIds: List<String>) {
        if (followedUsersIds.isEmpty()) {
            Log.d("HomeFragment", "No hay usuarios seguidos")
            postAdapter.updateData(emptyList())
            return
        }

        db.collection("posts")
            .whereIn("userId", followedUsersIds)
            .get()
            .addOnSuccessListener { documents ->
                val posts = mutableListOf<Post>()
                for (document in documents) {
                    val postId = document.id
                    val userId = document.getString("userId") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date()

                    val post = Post(postId, userId, imageUrl, title, description, timestamp)
                    posts.add(post)
                }
                postAdapter.updateData(posts)
                updatePostSaveStates(posts)
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error getting posts", exception)
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePostSaveStates(posts: MutableList<Post>) {
        val userId = UserUtils.getUserId(requireContext()).toString()
        val userSavedPostsRef = db.collection("users").document(userId).collection("savedPosts")

        userSavedPostsRef.get().addOnSuccessListener { documents ->
            val savedPostIds = documents.mapNotNull { it.id }
            posts.forEach { post ->
                post.isSaved = savedPostIds.contains(post.postId)
            }
            allPostsAdapter.notifyDataSetChanged()
            postAdapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Log.e("HomeFragment", "Error checking saved posts", exception)
        }
    }

    private fun toggleSavePost(post: Post, saveIconView: ImageView) {
        val userId = UserUtils.getUserId(requireContext()).toString()
        val postId = post.postId

        // Referencia a la colección de posts guardados del usuario
        val userSavedPostsRef =
            postId?.let {
                db.collection("users").document(userId).collection("savedPosts").document(it)
            }

        // Verificar si el post está guardado
        userSavedPostsRef?.get()?.addOnSuccessListener { document ->
            if (document.exists()) {
                // El post ya está guardado, por lo tanto, se debe eliminar
                userSavedPostsRef.delete()
                    .addOnSuccessListener {
                        Log.d("HomeFragment", "Post unsaved successfully")
                        SaveManager.removePost(requireContext(), postId)
                        updateSaveIcon(saveIconView, false)
                        post.isSaved = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeFragment", "Error unsaving post: ${e.message}")
                    }
            } else {
                // El post no está guardado, por lo tanto, se debe guardar
                val saveData = hashMapOf(
                    "postId" to postId,
                    "imageUrl" to post.imageUrl,
                    "timestamp" to System.currentTimeMillis()
                )
                userSavedPostsRef.set(saveData)
                    .addOnSuccessListener {
                        Log.d("HomeFragment", "Post saved successfully")
                        SaveManager.savePost(requireContext(), postId)
                        updateSaveIcon(saveIconView, true)
                        post.isSaved = true
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeFragment", "Error saving post: ${e.message}")
                    }
            }
        }?.addOnFailureListener { e ->
            Log.e("HomeFragment", "Error checking saved state: ${e.message}")
        }
    }

    private fun updateSaveIcon(saveIconView: ImageView, isSaved: Boolean) {
        if (isSaved) {
            saveIconView.setImageResource(R.drawable.ic_save)
        } else {
            saveIconView.setImageResource(R.drawable.ic_unsave)
        }
    }

    private fun isPostSaved(postId: String, callback: (Boolean) -> Unit) {
        val userId = UserUtils.getUserId(requireContext()).toString()
        val userSavedPostsRef = db.collection("users").document(userId).collection("savedPosts").document(postId)

        userSavedPostsRef.get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error checking saved state: ${e.message}")
                callback(false)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USER_FOLLOW && resultCode == Activity.RESULT_OK) {
            val userInfo = data?.getParcelableExtra<User>("user_info")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
