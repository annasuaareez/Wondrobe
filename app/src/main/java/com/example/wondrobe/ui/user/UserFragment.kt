package com.example.wondrobe.ui.user

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.wondrobe.R
import com.example.wondrobe.adapters.ImageProfileAdapter
import com.example.wondrobe.databinding.FragmentUserBinding
import com.example.wondrobe.ui.user.PostDetails.DetailsPost
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserFragment : Fragment(), ImageProfileAdapter.OnImageClickListener {

    private var _binding: FragmentUserBinding? = null

    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private lateinit var biography: String
    private lateinit var photoUrl: String
    private lateinit var bannerUrl: String
    private lateinit var followersCount: String
    private lateinit var followingCount: String
    private var userDetailsLoaded = false // Indica si los detalles del usuario ya han sido cargados

    private lateinit var textViewName: TextView
    private lateinit var textViewUsername: TextView
    private lateinit var textViewBiography: TextView
    private lateinit var textViewFollowersCount: TextView
    private lateinit var textViewFollowingCount: TextView
    private lateinit var circleImageView: ImageView
    private lateinit var bannerImageView: ImageView
    private lateinit var followingLayout: LinearLayout
    private lateinit var followersLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_user, container, false)

        textViewName = root.findViewById(R.id.textViewName)
        textViewUsername = root.findViewById(R.id.textViewUsername)
        textViewBiography = root.findViewById(R.id.textViewBiography)
        textViewFollowersCount = root.findViewById(R.id.textViewFollowersCount)
        textViewFollowingCount = root.findViewById(R.id.textViewFollowingCount)
        circleImageView = root.findViewById(R.id.circle)
        bannerImageView = root.findViewById(R.id.banner)
        followingLayout = root.findViewById(R.id.followingLayout)
        followersLayout = root.findViewById(R.id.followersLayout)

        root.findViewById<Button>(R.id.editProfileButton).setOnClickListener {
            redirectToEditUser()
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!userDetailsLoaded) {
            loadUserDetails()
            loadUserPosts()
        } else {
            updateUI()
        }

    }

    private fun loadUserDetails() {
        userId = UserUtils.getUserId(requireContext()).toString()

        Log.e("UserFragment", "UID del usuario: $userId")

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
                    bannerUrl = document.getString("bannerImage") ?: ""
                    followersCount = document.getLong("followersCount")?.toString() ?: "0"
                    followingCount = document.getLong("followingCount")?.toString() ?: "0"

                    // Indicar que los detalles del usuario han sido cargados
                    userDetailsLoaded = true

                    // Actualizar la interfaz de usuario incluyendo la foto
                    if (isAdded) {
                        updateUI()
                    }

                } else {
                    showAlertToast("User details not found.")
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error fetching user details: ${e.message}")
            }
    }

    private fun loadUserPosts() {
        userId = UserUtils.getUserId(requireContext()).toString()

        val db = FirebaseFirestore.getInstance()
        val postsCollection = db.collection("posts")

        postsCollection.whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING) // Ordenar por fecha de forma descendente (más reciente primero)
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
                    updateRecyclerView(imageUrls, postIds)
                } else {
                    // No hay imágenes para mostrar
                }
            }
            .addOnFailureListener { e ->
                // Manejar el error aquí
                Log.e("loadUserPosts", "Error fetching user posts: ${e.message}")
            }
    }

    /*private fun updateRecyclerView(imageUrls: List<String>) {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.postView)
        val numColumns = 2
        val imageAdapter = ImageProfileAdapter(requireContext(), imageUrls)
        val layoutManager = FlexboxLayoutManager(requireContext())

        recyclerView.setHasFixedSize(true)

        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.maxLine = numColumns
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = imageAdapter
    }*/

    private fun updateRecyclerView(imageUrls: List<String>, postIds: List<String>) {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.postView)
        val numColumns = 2
        val imageAdapter = ImageProfileAdapter(requireContext(), imageUrls, postIds, this)
        val layoutManager = StaggeredGridLayoutManager(numColumns, StaggeredGridLayoutManager.VERTICAL)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = imageAdapter
    }

    private fun updateUI() {
        val formattedUsername = "@$username"
        textViewName.text = firstName
        textViewUsername.text = formattedUsername
        textViewBiography.text = biography
        textViewFollowersCount.text = followersCount
        textViewFollowingCount.text = followingCount

        //showAlertDialog(photoUrl)
        // Verificar si el usuario tiene una foto de perfil
        if (photoUrl.isNotEmpty()) {
            // Cargar la imagen de perfil y aplicar la máscara circular
            Glide.with(requireContext()).clear(circleImageView)
            Glide.with(requireContext())
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
                .into(circleImageView)

            // Forzar la actualización de la vista después de cargar la imagen
            circleImageView.invalidate()
        }

        // Verificar si el usuario tiene un banner
        if (bannerUrl.isNotEmpty()) {
            // Cargar la imagen de perfil y aplicar la máscara circular
            Glide.with(requireContext()).clear(bannerImageView)
            Glide.with(requireContext())
                .load(bannerUrl)
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
                .into(bannerImageView)

            // Forzar la actualización de la vista después de cargar la imagen
            bannerImageView.invalidate()
        }

        // Mostrar los TextView una vez que se han cargado los datos del usuario
        textViewName.visibility = View.VISIBLE
        textViewUsername.visibility = View.VISIBLE
        textViewBiography.visibility = View.VISIBLE

        if (biography.isEmpty()) {
            // Si no hay biografía, mueve los layouts debajo del textViewUsername
            val paramsFollowing = followingLayout.layoutParams as ConstraintLayout.LayoutParams
            paramsFollowing.topToBottom = textViewUsername.id
            followingLayout.layoutParams = paramsFollowing

            val paramsFollowers = followersLayout.layoutParams as ConstraintLayout.LayoutParams
            paramsFollowers.topToBottom = textViewUsername.id
            followersLayout.layoutParams = paramsFollowers
        } else {
            // Si hay biografía, restaura las restricciones originales
            val paramsFollowing = followingLayout.layoutParams as ConstraintLayout.LayoutParams
            paramsFollowing.topToBottom = textViewBiography.id
            followingLayout.layoutParams = paramsFollowing

            val paramsFollowers = followersLayout.layoutParams as ConstraintLayout.LayoutParams
            paramsFollowers.topToBottom = textViewBiography.id
            followersLayout.layoutParams = paramsFollowers
        }
    }

    private fun redirectToEditUser() {
        val intent = Intent(activity, UserEdit::class.java)
        startActivityForResult(intent, REQUEST_CODE_EDIT_USER)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        // Notificar a la actividad para que elimine este fragmento
        //(activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT_USER && resultCode == Activity.RESULT_OK) {
            loadUserDetails()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onImageClick(postId: String) {
        userId = UserUtils.getUserId(requireContext()).toString()
        val intent = Intent(activity, DetailsPost::class.java)
        Log.e("Post Id and User Id", postId)
        intent.putExtra("PostID", postId)
        intent.putExtra("UserID", userId)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_EDIT_USER = 1001
    }
}
