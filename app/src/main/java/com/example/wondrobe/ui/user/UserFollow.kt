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

class UserFollow : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_follow)

        // Escuchar cambios en el tema de la aplicación
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        delegate.applyDayNight()

        // Obtener el modo de la noche actual
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        // Obtener la información del usuario seleccionado del Intent
        val selectedUser = intent.getParcelableExtra<User>("selected_user")

        Log.d("UserFollow", "Selected User: $selectedUser")

        // Configurar las vistas con la información del usuario
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

            // Verificar si el usuario tiene una imagen de perfil y cargarla utilizando Glide
            if (!user.profileImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.profileImage)
                    .placeholder(R.drawable.ic_user)
                    .transform(CircleCrop())
                    .into(imageView)
            } else {
                // Si no hay imagen, mostrar el icono predeterminado
                imageView.setImageResource(R.drawable.ic_user)
            }

            // Verificar si el usuario tiene un banner y cargarlo utilizando Glide si está disponible
            if (!user.bannerImage.isNullOrEmpty()) {
                Glide.with(this)
                    .load(user.bannerImage)
                    .into(bannerImageView)
            } else {
                // Si no hay banner disponible, establecer un color de fondo
                bannerImageView.setColorFilter(ContextCompat.getColor(this, R.color.light_blue_gray))
            }

            // Restaurar el estado de seguimiento del usuario desde SharedPreferences
            val savedFollowingState = SharedPreferencesManager.getFollowingState(this@UserFollow, user.username)
            user.isFollowing = savedFollowingState
            // Actualizar el texto y el fondo del botón según el estado guardado
            if (savedFollowingState) {
                followButton.text = "Following"
                followButton.setBackgroundResource(R.drawable.button_purple)
                followButton.setTextColor(Color.WHITE) // Cambiar el color del texto a blanco
            } else {
                followButton.text = "Follow"
                followButton.setBackgroundResource(R.drawable.button_white)
            }

            // Cambiar el texto del botón y el color de fondo al hacer clic en el botón
            followButton.setOnClickListener {
                // Cambiar el estado de seguimiento del usuario
                user.isFollowing = !user.isFollowing

                // Guardar el estado del botón en SharedPreferences
                SharedPreferencesManager.saveFollowingState(this@UserFollow, user.username, user.isFollowing)

                // Incrementar el contador de following del usuario actual y el contador de followers del usuario seguido
                val currentUserFollowingCount = SharedPreferencesManager.getFollowingCount(this@UserFollow)
                SharedPreferencesManager.saveFollowingCount(this@UserFollow, if (user.isFollowing) currentUserFollowingCount + 1 else currentUserFollowingCount - 1)

                val selectedUserFollowersCount = SharedPreferencesManager.getFollowersCount(this@UserFollow, user.username)
                SharedPreferencesManager.saveFollowersCount(this@UserFollow, user.username, if (user.isFollowing) selectedUserFollowersCount + 1 else selectedUserFollowersCount - 1)

                // Actualizar el texto y el fondo del botón según el estado actualizado
                if (user.isFollowing) {
                    followButton.text = "Following"
                    followButton.setBackgroundResource(R.drawable.button_purple)
                    followButton.setTextColor(Color.WHITE)
                } else {
                    followButton.text = "Follow"
                    followButton.setBackgroundResource(R.drawable.button_white)
                    followButton.setTextColor(Color.BLACK) // Restaurar el color del texto a negro
                }
            }

            if (user.biography.isNullOrEmpty()) {
                // Si no hay biografía, mueve los layouts de seguidores y seguidos debajo del nombre
                val paramsFollowing = followingLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowing.topToBottom = firstNameTextView.id
                followingLayout.layoutParams = paramsFollowing

                val paramsFollowers = followersLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowers.topToBottom = firstNameTextView.id
                followersLayout.layoutParams = paramsFollowers
            } else {
                // Si hay biografía, restaura las restricciones originales de los layouts de seguidores y seguidos
                val paramsFollowing = followingLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowing.topToBottom = biographyTextView.id
                followingLayout.layoutParams = paramsFollowing

                val paramsFollowers = followersLayout.layoutParams as ConstraintLayout.LayoutParams
                paramsFollowers.topToBottom = biographyTextView.id
                followersLayout.layoutParams = paramsFollowers
            }

        }

        // Modificar la flecha de retroceso según el modo de noche actual
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            // Modo oscuro
            arrowIcon.setImageResource(R.drawable.ic_back_white)
        } else {
            // Modo claro
            arrowIcon.setImageResource(R.drawable.ic_back)
        }

        // Agregar un OnClickListener al arrowIcon para volver a la página principal
        arrowIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            startActivity(intent)
            finish()
        }
    }
}
