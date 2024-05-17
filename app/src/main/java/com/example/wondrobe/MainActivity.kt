
package com.example.wondrobe

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.wondrobe.databinding.ActivityMainBinding
import com.example.wondrobe.ui.add.AddFragment
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_save,R.id.navigation_add, R.id.navigation_wardrobe, R.id.navigation_user
            )
        )

        // Configurar un ActionBar vacío
        setSupportActionBar(null)

        binding.navView.setupWithNavController(navController)

        // Ocultar los títulos y mostrar solo los iconos en la barra de navegación
        binding.navView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED

        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    val addFragment = AddFragment()
                    addFragment.show(supportFragmentManager, "add_fragment_tag")
                    true
                }
                else -> {
                    // Navegar a la opción seleccionada
                    item.onNavDestinationSelected(navController)
                }
            }
        }

        binding.navView.setOnItemReselectedListener { menuItem ->
            // Restablece el tinte de todos los íconos a su color predeterminado
            for (i in 0 until binding.navView.menu.size()) {
                val item = binding.navView.menu.getItem(i)
                val icon = item.icon
                icon?.clearColorFilter()
            }

            // Aplicar un tinte de color al ícono seleccionado
            val icon = menuItem.icon
            icon?.setColorFilter(ContextCompat.getColor(this, R.color.light_blue_gray), PorterDuff.Mode.SRC_IN)

            true
        }
    }
}
