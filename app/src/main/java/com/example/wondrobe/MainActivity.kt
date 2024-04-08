package com.example.wondrobe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.wondrobe.databinding.ActivityMainBinding
import com.example.wondrobe.ui.add.AddFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_calendar,R.id.navigation_add, R.id.navigation_wardrobe, R.id.navigation_user
            )
        )

        // Configurar un ActionBar vacío
        setSupportActionBar(null)

        binding.navView.setupWithNavController(navController)

        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    // Mostrar el fragmento de hoja
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
    }
}
