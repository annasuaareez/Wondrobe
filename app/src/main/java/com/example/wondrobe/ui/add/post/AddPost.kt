package com.example.wondrobe.ui.add.post

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.wondrobe.R

class AddPost : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        val backIcon = findViewById<ImageView>(R.id.backIconActivity)
        val imageView = findViewById<ImageView>(R.id.imageViewPost)
        val imageUriString = intent.getStringExtra("imageUri")

        backIcon.setOnClickListener {
            val intent = Intent(this, SelectPost::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
        }
    }
}
