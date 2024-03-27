package com.example.wondrobe.utils

import java.security.MessageDigest

class PasswordEncryptor {

    fun encryptPassword(password: String): String {
        val algorithm = "SHA-256"
        val digest = MessageDigest.getInstance(algorithm)
        digest.reset()
        val encryptedPassword = digest.digest(password.toByteArray()).joinToString("") {
            "%02x".format(it)
        }
        return encryptedPassword
    }
}
