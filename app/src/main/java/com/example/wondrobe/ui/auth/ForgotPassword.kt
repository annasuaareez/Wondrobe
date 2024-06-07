package com.example.wondrobe.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ForgotPassword : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var emailEditText: EditText
    private lateinit var sendButton: AppCompatButton
    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        backButton = findViewById(R.id.backButton)
        emailEditText = findViewById(R.id.emailEditText)
        sendButton = findViewById(R.id.sendPassword)
        alertDialogBuilder = AlertDialog.Builder(this)
        firestore = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        backButton.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                checkEmailAvailability(email)
            } else {
                showAlertDialog("Invalid Email", "Please enter a valid email address.")
            }
        }
    }

    private fun checkEmailAvailability(email: String) {
        // Aquí asumimos que tienes una colección llamada "users" en Firestore
        val usersCollection = firestore.collection("users")
        usersCollection.whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // El correo electrónico está registrado, procedemos a enviar el correo de restablecimiento
                    sendPasswordResetEmail(email)
                } else {
                    showAlertDialog("Email Not Found", "This email is not registered. Please enter a registered email.")
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores
                showAlertDialog("Error", "An error occurred while checking email availability: ${e.message}")
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Correo de restablecimiento enviado con éxito
                    Toast.makeText(this, "Password reset email sent successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Manejar el error
                    Toast.makeText(this, "Error resetting password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showAlertDialog(title: String, message: String) {
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK", null)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}
