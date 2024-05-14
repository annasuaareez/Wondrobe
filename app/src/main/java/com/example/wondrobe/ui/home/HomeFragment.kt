    package com.example.wondrobe.ui.home

    import android.animation.ObjectAnimator
    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.view.animation.AccelerateDecelerateInterpolator
    import android.widget.ListView
    import android.widget.SearchView
    import androidx.fragment.app.Fragment
    import com.example.wondrobe.data.User
    import com.example.wondrobe.databinding.FragmentHomeBinding
    import com.example.wondrobe.ui.user.UserFollow
    import com.example.wondrobe.utils.UserUtils
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore

    class HomeFragment : Fragment() {

        private var _binding: FragmentHomeBinding? = null
        private val binding get() = _binding!!

        private lateinit var auth: FirebaseAuth
        private lateinit var db: FirebaseFirestore
        private lateinit var currentUserUid: String
        private lateinit var searchView: SearchView
        private lateinit var listView: ListView
        private lateinit var blackOverlay: View

        companion object {
            private const val REQUEST_USER_FOLLOW = 1001
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            val root: View = binding.root

            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            currentUserUid = auth.currentUser?.uid ?: ""

            val userId = UserUtils.getUserId(requireContext()).toString()

            Log.e("HomeFragment", "UID del usuario: $userId")

            searchView = binding.searchView
            listView = binding.listUsers
            blackOverlay = binding.blackOverlay

            val forYouButton = binding.forYouButton
            val followingButton = binding.followingButton
            val forYouIndicator = binding.forYouIndicator
            val followingIndicator = binding.followingIndicator

            searchView.setOnSearchClickListener {
                blackOverlay.visibility = View.VISIBLE
            }

            searchView.setOnCloseListener {
                blackOverlay.visibility = View.GONE
                updateListView(emptyList(), listView)
                //listView.visibility = View.GONE
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
            }

            followingButton.setOnClickListener {
                animateIndicatorChange(followingIndicator, forYouIndicator)
            }

            return root
        }

        override fun onPause() {
            super.onPause()
            // Si el SearchView está abierto y tiene texto escrito
            if (!searchView.isIconified && searchView.query.isNotEmpty()) {
                searchView.setQuery("", false)
                searchView.isIconified = true
                blackOverlay.visibility = View.GONE
                updateListView(emptyList(), listView)
                listView.visibility = View.GONE
            }
        }

        private fun searchUsers(query: String, listView: ListView) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThan("username", query + "\uf8ff")
                //.whereNotEqualTo("uid", currentUserUid)
                .limit(8)
                .get()
                .addOnSuccessListener { documents ->
                    val usersList = mutableListOf<User>()
                    for (document in documents) {
                        val uid = document.id
                        val email = document.getString("email") ?: ""
                        val username = document.getString("username") ?: ""
                        val firstName = document.getString("firstName") ?: ""
                        val biography = document.getString("biography") ?: ""
                        val profileImage = document.getString("profileImage") ?: ""
                        val bannerImage = document.getString("bannerImage") ?: ""
                        //val isAdmin = document.getBoolean("isAdmin") ?: false
                        val followersCount = document.getLong("followersCount")?.toInt() ?: 0
                        val followingCount = document.getLong("followingCount")?.toInt() ?: 0

                        //Log.e("IMAGEN", profileImage)

                        val user = User(uid, email,username, firstName,biography,"", profileImage, bannerImage,
                            false,followersCount, followingCount)
                        usersList.add(user)
                    }
                    updateListView(usersList, listView)
                }
                .addOnFailureListener { exception ->
                    // Handle errors
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

            listView.setOnItemClickListener { parent, view, position, id ->
                val selectedUser = usersList[position]
                val intent = Intent(requireContext(), UserFollow::class.java)
                intent.putExtra("selected_user", selectedUser)
                startActivityForResult(intent, REQUEST_USER_FOLLOW)
            }
        }

        private fun animateIndicatorChange(showIndicator: View, hideIndicator: View) {
            val hideAnimator = ObjectAnimator.ofFloat(hideIndicator, "alpha", 1f, 0f)
            hideAnimator.duration = 250 // Duration of the animation in milliseconds
            hideAnimator.interpolator = AccelerateDecelerateInterpolator()

            val showAnimator = ObjectAnimator.ofFloat(showIndicator, "alpha", 0f, 1f)
            showAnimator.duration = 250 // Duration of the animation in milliseconds
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

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_USER_FOLLOW && resultCode == Activity.RESULT_OK) {
                // Aquí obtienes los datos devueltos por UserFollowActivity
                val userInfo = data?.getParcelableExtra<User>("user_info")
                // Haz lo que necesites con la información del usuario
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
