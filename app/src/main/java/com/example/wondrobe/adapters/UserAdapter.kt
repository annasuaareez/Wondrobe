package com.example.wondrobe.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wondrobe.R
import com.example.wondrobe.data.User

class UserAdapter(private val context: Context, private val users: List<User>) :
    ArrayAdapter<User>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

           val user = getItem(position)

        val imageView = view.findViewById<ImageView>(R.id.user_image)
        val usernameTextView = view.findViewById<TextView>(R.id.username)
        val firstNameTextView = view.findViewById<TextView>(R.id.first_name)

        // Verificar si user no es nulo antes de acceder a sus propiedades
        user?.let {
            Log.d("UserAdapter", "Username: ${it.username}, First Name: ${it.firstName}, URL: ${it.profileImage}")

            if (it.profileImage.isNullOrEmpty()) {
                //si no hay imagen ponemos el icono
                imageView.setImageResource(R.drawable.ic_user_white)
            } else {
                Glide.with(context)
                    .load(it.profileImage)
                    .placeholder(R.drawable.ic_user)
                    .transform(CircleCrop())
                    .into(imageView)
            }

            usernameTextView.text = it.username
            firstNameTextView.text = it.firstName
        }

        return view
    }
}
