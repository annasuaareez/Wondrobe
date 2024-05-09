package com.example.wondrobe.ui.auth

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.wondrobe.R

class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        val acceptButton = findViewById<Button>(R.id.acceptButton)
        acceptButton.setOnClickListener {
            // Establecer el resultado como OK
            setResult(Activity.RESULT_OK)
            // Finalizar esta actividad
            finish()
        }
    }
}