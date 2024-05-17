package com.example.wondrobe.ui.auth

import ValidationUtils
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import com.example.wondrobe.MainActivity
import com.example.wondrobe.R
import com.example.wondrobe.utils.PasswordEncryptor
import com.example.wondrobe.utils.PasswordVisibility
import com.example.wondrobe.utils.UserUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider.*
import com.google.firebase.firestore.FirebaseFirestore

class LogIn : AppCompatActivity() {
    private val RC_SIGN_IN = 9001

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        supportActionBar?.hide()

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        // Cambiar la imagen de acuerdo con el tema actual
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        val isotypeImageView = findViewById<ImageView>(R.id.isotype)
        val changeToSign = findViewById<TextView>(R.id.changeSignIn)
        val buttonLogIn = findViewById<AppCompatButton>(R.id.buttonLogIn)
        val logInWithGoogle = findViewById<LinearLayout>(R.id.loginWithGoogle)
        val passwordEditText = findViewById<EditText>(R.id.passwordLogInEditText)
        val passwordVisibilityButton = findViewById<ImageView>(R.id.passwordVisibilityButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

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

        //Cambia de vista al Sign up
        changeToSign.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        buttonLogIn.setOnClickListener {
            val usernameEditText = findViewById<EditText>(R.id.usernameLoginEditText)
            val passwordEditText = findViewById<EditText>(R.id.passwordLogInEditText)

            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            loginUser(username, password)
        }

        logInWithGoogle.setOnClickListener {
            if (isConnect(this)) {
                // Cerrar sesión de Google para permitir al usuario seleccionar otra cuenta
                googleSignInClient.signOut().addOnCompleteListener {
                    // Una vez que se ha cerrado la sesión, iniciar el flujo de inicio de sesión
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, RC_SIGN_IN)
                }
            } else {
                showAlertDialog("No internet connection")
            }
        }

        forgotPassword.setOnClickListener {
            // Aquí puedes iniciar la otra actividad o realizar la acción necesaria
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                //showAlertToast("Google sign in failed: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val googleUser = account ?: return@addOnCompleteListener

                    val email = googleUser.email.orEmpty()
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    if (isNewUser) {
                        // Eliminar la cuenta de Google si el usuario no está registrado en Firebase
                        FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
                                showAlertDialog("User not found in. Please sign up.")
                            } else {
                                showAlertToast("Error deleting account: ${deleteTask.exception?.message}")
                            }
                        }
                    } else {
                        // Si el usuario de Google ya existe en Firebase Auth, verificar si está registrado en Firestore
                        checkIfUserExistsInFirestore(email)
                    }
                }
            }
    }
    
    private fun loginUser(
        username: String,
        password: String
    ) {
        val validationResult = ValidationUtils.validateFieldsLogIn(username, password)

        if (validationResult == ValidationUtils.ValidationResult.SUCCESS) {
            val encryptedPassword = PasswordEncryptor().encryptPassword(password)
            Log.e("Contraseña incresada","")

            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")

            usersCollection.whereEqualTo("username", username)
                //.whereEqualTo("password", encryptedPassword)
                .get()
                .addOnSuccessListener { documents ->
                    val encryptedPasswordDB = documents.documents[0].getString("password")

                    if (encryptedPassword == encryptedPasswordDB) {
                        // Credenciales válidas, redirigir al usuario al MainActivity
                        val userId = documents.documents[0].id
                        UserUtils.saveUserId(this, userId)
                        Log.e("Log In", userId)
                        showAlertToast("Successful login")
                        redirectToMainPage()
                    } else {
                        // Credenciales inválidas, mostrar diálogo
                        showAlertDialog("Credentials are not valid")
                    }
                }
                .addOnFailureListener { e ->
                    // Manejar errores de Firestore
                    showAlertToast("Error verifying credentials: ${e.message}")
                }
        } else {
            ValidationUtils.showInvalidFieldsAlert(this, validationResult)
        }
    }

    @Deprecated("Deprecated in Java")
    fun isConnect(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkIfUserExistsInFirestore(email: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Usuario encontrado en Firestore, obtener su ID y redirigir al MainActivity
                    val userId = documents.documents[0].id
                    UserUtils.saveUserId(this, userId)
                    showAlertToast("Successful login")
                    redirectToMainPage()
                } else {
                    // Usuario no encontrado en Firestore
                    showAlertToast("User not found in database")
                }
            }
            .addOnFailureListener { e ->
                // Manejar errores de Firestore
                showAlertDialog("Error retrieving user data: ${e.message}")
            }
    }

    private fun redirectToMainPage(){
        val intent = Intent(this@LogIn, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlertDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Invalid credentials")
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}
