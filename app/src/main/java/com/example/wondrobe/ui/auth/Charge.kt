package com.example.wondrobe.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.wondrobe.R

class Charge: AppCompatActivity() {
    private val splashTimeOut: Long = 1500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(splashTimeOut)
        installSplashScreen()
        setContentView(R.layout.activity_charge)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val buttonLogIn = findViewById<Button>(R.id.buttonLogIn)
        val buttonSignUp = findViewById<Button>(R.id.buttonSignUp)

        buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        buttonLogIn.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
}
