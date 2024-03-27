package com.example.wondrobe.ui.auth

import ValidationUtils
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.MainActivity
import com.example.wondrobe.R
import com.example.wondrobe.data.User
import com.example.wondrobe.utils.PasswordEncryptor
import com.example.wondrobe.utils.PasswordVisibility
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private val RC_SIGN_IN = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()

        // Cambiar la imagen de acuerdo con el tema actual
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        val isotypeImageView = findViewById<ImageView>(R.id.isotype)
        val changeToLog = findViewById<TextView>(R.id.changeLogIn)
        val buttonSignUp = findViewById<AppCompatButton>(R.id.buttonSignUp)
        val signUpWithGoogle = findViewById<LinearLayout>(R.id.singUpWithGoogle)
        val passwordEditText = this.findViewById<EditText>(R.id.passwordSignUpEditText)
        val passwordVisibilityButton = findViewById<ImageView>(R.id.passwordVisibilityButton)

        PasswordVisibility(passwordEditText, passwordVisibilityButton)

        // Configurar el inicio de sesión con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Escuchar cambios en el tema de la aplicación
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        delegate.applyDayNight()

        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            // Modo oscuro
            isotypeImageView.setImageResource(R.drawable.isotype_white)
        } else {
            // Modo claro
            isotypeImageView.setImageResource(R.drawable.isotype_black)
        }

        // Cambiar de vista al Log in
        changeToLog.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        buttonSignUp.setOnClickListener {
            val firstName = findViewById<EditText>(R.id.firstNameEditText).text.toString()
            val username = findViewById<EditText>(R.id.usernameSignUpEditText).text.toString()
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordSignUpEditText).text.toString()

            registerUser(email, username, firstName, password)
        }

        signUpWithGoogle.setOnClickListener {
            if (ValidationUtils.isConnect(this)) {
                // Cerrar sesión de Google para permitir al usuario seleccionar otra cuenta
                googleSignInClient.signOut().addOnCompleteListener {
                    // Una vez que se ha cerrado la sesión, iniciar el flujo de inicio de sesión
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            } else {
                showAlertToast("No internet connection")
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RC_SIGN_IN -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    e.printStackTrace()
                    //e.message?.let { showAlertDialog(it) }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Verificar si el usuario ya está registrado con la dirección de correo electrónico asociada a su cuenta de Google
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (!isNewUser) {
                        // El usuario ya está registrado, mostrar mensaje de advertencia
                        showAlertToast("This account has already been registered")
                        // Cerrar la sesión de Firebase ya que el usuario no se ha autenticado correctamente
                        FirebaseAuth.getInstance().signOut()
                    } else {
                        // El usuario es nuevo, proceder con el registro o inicio de sesión
                        //showAlertDialog("Login successful")
                        val intent = Intent(this@SignUp, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    }
                } else {
                    // Error al autenticar con Firebase
                    task.exception?.printStackTrace()
                    //task.exception?.message?.let { message -> showAlertDialog(message) }
                }
            }
    }

    private fun registerUser(
        email: String,
        username: String,
        firstName: String,
        password: String
    ) {
        val validationResult = ValidationUtils.validateFieldsSignUp(email, username, firstName, password)

        if (validationResult == ValidationUtils.ValidationResult.SUCCESS) {
            checkEmailAvailability(email) { isAvailableEmail ->
                if (isAvailableEmail) {
                    checkUsernameAvailability(username) { isAvailableUsername ->
                        if (isAvailableUsername) {
                            // Tanto el correo electrónico como el nombre de usuario están disponibles, proceder con el registro
                            val encryptedPassword = PasswordEncryptor().encryptPassword(password)
                            val user = User(email = email, username = username, firstName = firstName, password = encryptedPassword)

                            val db = FirebaseFirestore.getInstance()

                            db.collection("users")
                                .add(user)
                                .addOnSuccessListener {
                                    // Mostrar un mensaje de éxito o realizar otras acciones necesarias
                                    showAlertToast("User successfully registered")
                                    redirectToLogIn()
                                }
                                .addOnFailureListener { e ->
                                    // Manejar el error en caso de que falle la escritura en Firestore
                                    showAlertToast("Error registering user: ${e.message}")
                                }
                        } else {
                            // Mostrar AlertDialog indicando que el nombre de usuario ya está en uso
                            showAlertDialog("Username is already taken. Please choose another one.")
                        }
                    }
                } else {
                    // Mostrar AlertDialog indicando que el correo electrónico ya está registrado
                    showAlertDialog("Email is already registered.")
                }
            }
        } else {
            ValidationUtils.showInvalidFieldsAlert(this, validationResult)
        }
    }

    private fun checkEmailAvailability(email: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Verificar disponibilidad del correo electrónico
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                showAlertToast("Error checking email availability: ${e.message}")
                // Llamar al callback con un valor predeterminado en caso de error
                callback(false)
            }
    }

    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Verificar disponibilidad del nombre de usuario
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                showAlertToast("Error checking username availability: ${e.message}")
                // Llamar al callback con un valor predeterminado en caso de error
                callback(false)
            }
    }

    private fun redirectToLogIn() {
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlertDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Item already registered")
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}