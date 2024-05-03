package com.example.wondrobe.ui.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wondrobe.R
import com.example.wondrobe.data.User

class UserFollow : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_follow)

        // Cambiar la imagen de acuerdo con el tema actual
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        // Obtener la información del usuario seleccionado del Intent
        val selectedUser = intent.getParcelableExtra<User>("selected_user")

        Log.d("UserFollow", "Selected User: $selectedUser")

        // Configurar las vistas con la información del usuario
        val imageView = findViewById<ImageView>(R.id.profileImage)
        val username = findViewById<TextView>(R.id.username)
        val firstName = findViewById<TextView>(R.id.firstName)
        val biography = findViewById<TextView>(R.id.biography)
        val banner = findViewById<ImageView>(R.id.banner)
        val followers = findViewById<TextView>(R.id.followersCount)
        val following = findViewById<TextView>(R.id.followingCount)
        val arrowIcon = findViewById<ImageView>(R.id.arrowIcon)

        selectedUser?.let { user ->
            username.text = user.username
            firstName.text = user.firstName
            biography.text = user.biography
            followers.text = user.followersCount.toString()
            following.text = user.followingCount.toString()

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
                    .into(banner)
            } else {
                // Si no hay banner disponible s queda con el color
                banner.setColorFilter(resources.getColor(R.color.light_blue_gray))
            }
        }

        // Escuchar cambios en el tema de la aplicación
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        delegate.applyDayNight()

        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            // Modo oscuro
            arrowIcon.setImageResource(R.drawable.ic_back_white)
        } else {
            // Modo claro
            arrowIcon.setImageResource(R.drawable.ic_back)
        }

    }
}
