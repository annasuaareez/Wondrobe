package com.example.wondrobe

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView

class PasswordVisibilityToggle(private val editText: EditText, private val imageView: ImageView) {

    private var isPasswordVisible = false

    init {
        // Agregar un listener al ImageView para manejar el cambio de visibilidad
        imageView.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    // Método para cambiar la visibilidad de la contraseña
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Si la contraseña es visible, ocultarla
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_hidden_24)
        } else {
            // Si la contraseña está oculta, hacerla visible
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_visibility_24)
        }
        // Cambiar el estado de la contraseña
        isPasswordVisible = !isPasswordVisible
        // Mover el cursor al final del texto
        editText.setSelection(editText.text.length)
    }
}