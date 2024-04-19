package com.example.wondrobe.ui.home

//noinspection SuspiciousImport
import android.R
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.wondrobe.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar Firebase Auth y Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Obtener el identificador único del usuario actual
        currentUserUid = auth.currentUser?.uid ?: ""

        val searchView = binding.searchView
        //val listView = binding.listUsers

        val forYouButton = binding.forYouButton
        val followingButton = binding.followingButton
        val forYouIndicator = binding.forYouIndicator
        val followingIndicator = binding.followingIndicator

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it != currentUserUid && it != auth.currentUser?.displayName) { // Evitar búsqueda de uno mismo
                        //searchUsers(it, listView)
                    } else {
                        //updateListView(emptyList(), listView) // Si coincide, muestra una lista vacía
                        searchView.setQuery("", false)
                    }
                }
                return true
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

    private fun searchUsers(query: String, listView: ListView) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereGreaterThanOrEqualTo("username", query)
            .whereLessThan("username", query + "\uf8ff")
            .limit(8)
            .get()
            .addOnSuccessListener { documents ->
                val usersList = mutableListOf<String>()
                for (document in documents) {
                    val username = document.getString("username") ?: ""
                    val firstName = document.getString("firstName") ?: ""
                    usersList.add("$username - $firstName")
                }
                updateListView(usersList, listView)
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.e("HomeFragment", "Error searching users", exception)
            }
    }

    private fun updateListView(usersList: List<String>, listView: ListView) {
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, usersList)
        listView.adapter = adapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
