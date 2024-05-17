package com.example.wondrobe.ui.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class ForgotPassword : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var emailEditText: EditText
    private lateinit var verificationCodeEditText: EditText
    private lateinit var sendButton: AppCompatButton
    private lateinit var alertDialogBuilder: AlertDialog.Builder
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var resetCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        backButton= findViewById(R.id.backButton)
        emailEditText = findViewById(R.id.emailEditText)
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText)
        sendButton = findViewById(R.id.sendPassword)
        alertDialogBuilder = AlertDialog.Builder(this)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        backButton.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }

        sendButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (ValidationUtils.isEmailValid(email)) {
                checkEmailAvailability(email)
            } else {
                showAlertDialog("Invalid Email", "Please enter a valid email address.")
            }
        }
    }

    private fun checkEmailAvailability(email: String) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // El correo electrónico está registrado, procedemos a enviar el código de verificación
                    sendVerificationCode(email)
                } else {
                    showAlertDialog("Email Not Found", "This email is not registered. Please enter a registered email.")
                }
            }
            .addOnFailureListener { e ->
                showAlertDialog("Database Error", "An error occurred while accessing the database.")
            }
    }

    private fun sendVerificationCode(email: String) {
        // Generar código de verificación aleatorio
        resetCode = generateRandomCode()

        // Enviar correo electrónico de verificación con el código resetCode a la dirección de correo electrónico del usuario
        val subject = "Wondrobe Password Reset"
        val message = "Your password reset code is: $resetCode"
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(intent, "Choose an email client"))

        // Cambiar la visibilidad del campo de ingreso del código de verificación a visible
        verificationCodeEditText.visibility = View.VISIBLE

        // Mostrar el campo para ingresar el código de verificación
        verificationCodeEditText.isEnabled = true
        verificationCodeEditText.requestFocus()

        // Cambiar el texto del botón para permitir al usuario confirmar el código
        sendButton.text = "Confirm Code"

        sendButton.setOnClickListener {
            val enteredCode = verificationCodeEditText.text.toString().trim()
            if (enteredCode == resetCode) {
                // El código ingresado por el usuario coincide, permitir al usuario restablecer la contraseña
                // Llamar a la función para restablecer la contraseña
                resetPassword(email)
            } else {
                showAlertDialog("Invalid Code", "Please enter the correct verification code.")
            }
        }
    }

    private fun resetPassword(email: String) {
        // Aquí deberías implementar la lógica para permitir al usuario restablecer la contraseña.
        // Por ejemplo:
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Contraseña restablecida con éxito
                    Toast.makeText(this, "Password reset email sent successfully.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Manejar el error
                    Toast.makeText(this, "Error resetting password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores de Firebase
                showAlertDialog("Error", "An error occurred while resetting password.")
            }
    }

    private fun generateRandomCode(): String {
        val min = 10000
        val max = 99999
        return (min + Random().nextInt(max - min)).toString()
    }

    private fun showAlertDialog(title: String, message: String) {
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK", null)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}