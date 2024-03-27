package com.example.wondrobe

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.GoogleAuthProvider.*

class LogIn : AppCompatActivity() {
    private val RC_SIGN_IN = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Cambiar la imagen de acuerdo con el tema actual
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK

        val isotypeImageView = findViewById<ImageView>(R.id.isotype)
        val changeToSign = findViewById<TextView>(R.id.changeSignIn)
        val logInWithGoogle = findViewById<LinearLayout>(R.id.loginWithGoogle)

        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val passwordVisibilityButton = findViewById<ImageView>(R.id.passwordVisibilityButton)

        PasswordVisibilityToggle(passwordEditText, passwordVisibilityButton)

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
    }

    fun isConnect(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                showAlertDialog("Google sign in failed: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        showAlertDialog("This account is not registered")
                    } else {
                        //showAlertDialog("Login successful")
                        val intent = Intent(this@LogIn, MainActivity::class.java)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    }
                } else {
                    showAlertDialog("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun showAlertDialog(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}