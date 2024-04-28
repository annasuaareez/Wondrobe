package com.example.wondrobe.ui.auth

import ValidationUtils
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backButton = findViewById<ImageView>(R.id.backButton)
        val sendButton = findViewById<AppCompatButton>(R.id.sendPassword)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)

        backButton.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (ValidationUtils.isEmailValid(email)) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { passwordResetTask ->
                        if (passwordResetTask.isSuccessful) {
                            // Correo de restablecimiento de contraseña enviado con éxito
                            Toast.makeText(
                                this,
                                "Password reset email sent successfully.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Regresa a la pantalla de inicio de sesión
                            val intent = Intent(this, LogIn::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            // Maneja el error
                            Toast.makeText(
                                this,
                                "Error sending password reset email: ${passwordResetTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                // Muestra un AlertDialog indicando que el correo no es válido
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Invalid Email")
                alertDialogBuilder.setMessage("Please enter a valid email address.")
                alertDialogBuilder.setPositiveButton("OK", null)
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }

    }
}

