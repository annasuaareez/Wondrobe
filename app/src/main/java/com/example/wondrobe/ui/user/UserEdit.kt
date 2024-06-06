package com.example.wondrobe.ui.user

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.wondrobe.R
import com.example.wondrobe.databinding.ActivityUserEditBinding
import com.example.wondrobe.ui.auth.LogIn
import com.example.wondrobe.utils.SessionManager
import com.example.wondrobe.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class UserEdit : AppCompatActivity() {

    // Definir constantes para solicitar permisos
    private val CAMERA_PERMISSION_CODE = 100
    private val PROFILE_CAMERA_REQUEST_CODE = 101
    private val PROFILE_GALLERY_REQUEST_CODE = 102

    private val CAMERA_PERMISSION_CODE_BANNER = 103
    private val PROFILE_CAMERA_REQUEST_CODE_BANNER = 104
    private val PROFILE_GALLERY_REQUEST_CODE_BANNER = 105

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var loadingProgressBar: ProgressBar
    private var pendingOperationsCount: Int = 0
    private lateinit var userId: String
    private lateinit var firstName: String
    private lateinit var username: String
    private lateinit var biography: String
    private lateinit var photoUrl: String
    private lateinit var bannerUrl: String

    private var cameraOptionsDialog: Dialog? = null
    private var cameraOptionsDialogBanner: Dialog? = null
    private var newPhotoBitmap: Bitmap? = null
    private var newBannerBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        loadUserDetails()

        loadingProgressBar = findViewById(R.id.loadingProgressBar)

        binding.cameraIconProfile.setOnClickListener {
            showPhotoOptionsDialog()
        }

        binding.cameraIconBanner.setOnClickListener {
            showBannerOptionsDialog()
        }

        binding.saveChange.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE

            val newUsername = binding.newUsernameEditText.text.toString()
            val newFirstName = binding.newNameEditText.text.toString()
            val newBiography = binding.newBiographyEditText.text.toString()

            if (validateInputs(newUsername, newFirstName, newBiography)) {
                pendingOperationsCount = 0

                // Verificar si el nuevo nombre de usuario es diferente al nombre de usuario actual
                if (newUsername != username) {
                    // El usuario desea cambiar su nombre de usuario
                    saveUserData(newUsername, newFirstName, newBiography)
                    pendingOperationsCount++
                } else {
                    // El usuario no desea cambiar su nombre de usuario, solo actualiza la información adicional
                    saveAdditionalUserData(newFirstName, newBiography)
                    pendingOperationsCount++
                }
            } else {
                loadingProgressBar.visibility = View.GONE
            }
        }

        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }

        binding.logOutButton.setOnClickListener {
            // Cerrar la sesión del usuario
            //FirebaseAuth.getInstance().signOut()

            // Limpiar sesión
            SessionManager.clearSession(this)

            // Iniciar la actividad de inicio de sesión (LogIn)
            val intent = Intent(this, LogIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }

    private fun loadUserDetails() {
        userId = UserUtils.getUserId(this).toString()

        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    firstName = document.getString("firstName") ?: ""
                    username = document.getString("username") ?: ""
                    biography = document.getString("biography") ?: ""
                    photoUrl = document.getString("profileImage") ?: ""
                    bannerUrl = document.getString("bannerImage") ?: ""

                    // Actualizar los EditText con los valores recuperados
                    binding.newNameEditText.setText(firstName)
                    binding.newUsernameEditText.setText(username)
                    binding.newBiographyEditText.setText(biography)

                    // Cargar la imagen de perfil si existe
                    if (photoUrl.isNotEmpty()) {
                        // Cargar la imagen de perfil y aplicar la máscara circular
                        Glide.with(this)
                            .load(photoUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // Manejar el error al cargar la imagen
                                    //showAlertToast("Error loading image: ${e?.message}")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    dataSource: com.bumptech.glide.load.DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    //showAlertToast("Imagen carga bien")
                                    return false
                                }

                            })
                            .transform(CircleCrop())
                            .into(binding.editUserCircle)
                    }

                    // Cargar la imagen del banner si existe
                    if (bannerUrl.isNotEmpty()) {
                        // Cargar la imagen de perfil y aplicar la máscara circular
                        Glide.with(this)
                            .load(bannerUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // Manejar el error al cargar la imagen
                                    //showAlertToast("Error loading image: ${e?.message}")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    dataSource: com.bumptech.glide.load.DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    //showAlertToast("Imagen carga bien")
                                    return false
                                }

                            })
                            .into(binding.banner)
                    }
                } else {
                    showAlertToast("User details not found.")
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error fetching user details: ${e.message}")
            }
    }

    private fun validateInputs(username: String, firstName: String, biography: String): Boolean {
        val trimmedUsername = username.trim()
        val trimmedFirstName = firstName.trim()
        val trimmedBiography = biography.trim()

        if (trimmedUsername.isEmpty() || trimmedFirstName.isEmpty()) {
            return false
        }
        if (trimmedUsername.length > 20 || trimmedFirstName.length > 40 || trimmedBiography.length > 160) {
            return false
        }
        if (containsWhiteSpace(trimmedUsername)) {
            return false
        }
        if (containsEmojis(trimmedUsername)) {
            showEmojiAlert()
            return false
        }
        return true
    }

    private fun containsWhiteSpace(text: String): Boolean {
        return text.contains(" ")
    }

    private fun containsEmojis(text: String): Boolean {
        val emojiRegex = Regex("[\\p{So}]")
        return emojiRegex.containsMatchIn(text)
    }

    private fun showEmojiAlert() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Invalid Input")
        alertDialogBuilder.setMessage("Username cannot contain emojis.")
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun saveUserData(username: String, firstName: String, biography: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        // Verificar si el nuevo nombre de usuario es diferente al nombre de usuario actual
        if (username != this.username) {
            if (containsWhiteSpace(username)) {
                showAlertDialog("Username cannot contain trailing or leading spaces.")
                return
            }
            if (containsEmojis(username)) {
                showAlertDialog("Username cannot contain emojis.")
                return
            }
        }

        usersCollection.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // El nombre de usuario no está registrado, se puede guardar
                    val user = hashMapOf<String, Any>(
                        "username" to username,
                        "firstName" to firstName,
                        "biography" to biography,
                        "profileImage" to photoUrl,
                        "bannerImage" to bannerUrl
                    )

                    usersCollection.document(userId)
                        .update(user)
                        .addOnSuccessListener {
                            pendingOperationsCount--

                            showAlertToast("User data updated successfully.")

                            // Guardar la foto en el almacenamiento de Firebase y obtener la URL
                            newPhotoBitmap?.let {
                                savePhotoToFirebaseStorage(it)
                            }

                            newBannerBitmap?.let {
                                saveBannerToFirebaseStorage(it)
                            }

                            val resultIntent = Intent()
                            resultIntent.putExtra("username", username)
                            resultIntent.putExtra("firstName", firstName)
                            resultIntent.putExtra("biography", biography)
                            resultIntent.putExtra("profileImage", photoUrl)
                            resultIntent.putExtra("bannerImage", bannerUrl)
                            setResult(Activity.RESULT_OK, resultIntent)

                            if (pendingOperationsCount == 0) {
                                loadingProgressBar.visibility = View.GONE
                                finish()
                            }
                        }
                        .addOnFailureListener { e ->
                            loadingProgressBar.visibility = View.GONE
                            showAlertToast("Error updating user data: ${e.message}")
                            finish()
                        }
                } else {
                    // El nombre de usuario ya está registrado
                    showAlertDialog("Username already exists. Please choose a different one.")
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error checking username availability: ${e.message}")
            }
    }

    private fun saveAdditionalUserData(firstName: String, biography: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        // Verificar si el nuevo nombre de usuario es diferente al nombre de usuario actual
        if (username != this.username) {
            if (containsWhiteSpace(username)) {
                showAlertDialog("Username cannot contain trailing or leading spaces.")
                return
            }
            if (containsEmojis(username)) {
                showAlertDialog("Username cannot contain emojis.")
                return
            }
        }

        val user = hashMapOf<String,Any>(
            "firstName" to firstName,
            "biography" to biography,
            "profileImage" to photoUrl,
            "bannerImage" to bannerUrl
        )

        usersCollection.document(userId)
            .update(user)
            .addOnSuccessListener {
                pendingOperationsCount--

                showAlertToast("User data updated successfully.")

                // Guardar la foto en el almacenamiento de Firebase y obtener la URL
                newPhotoBitmap?.let {
                    savePhotoToFirebaseStorage(it)
                }

                newBannerBitmap?.let {
                    saveBannerToFirebaseStorage(it)
                }

                val resultIntent = Intent()
                resultIntent.putExtra("username", username)
                resultIntent.putExtra("firstName", firstName)
                resultIntent.putExtra("biography", biography)
                resultIntent.putExtra("profileImage", photoUrl)
                resultIntent.putExtra("bannerImage", bannerUrl)
                setResult(Activity.RESULT_OK, resultIntent)

                if (pendingOperationsCount == 0) {
                    loadingProgressBar.visibility = View.GONE
                    finish()
                }
            }
            .addOnFailureListener { e ->
                loadingProgressBar.visibility = View.GONE
                showAlertToast("Error updating user data: ${e.message}")
                finish()
            }
    }

    private fun savePhotoToFirebaseStorage(bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = UserUtils.getUserId(this).toString()
        val imagesRef = storageRef.child("/users/$userId/profile/photo_profile.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                photoUrl = uri.toString()
                // Guardar la URL de la foto en la base de datos de Firebase Firestore
                savePhotoUrlToFirestore(photoUrl)
            }.addOnFailureListener {
                showAlertToast("Error getting photo URL: ${it.message}")
            }
        }.addOnFailureListener { exception ->
            showAlertToast("Error uploading photo: ${exception.message}")
        }
    }

    private fun saveBannerToFirebaseStorage(bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = UserUtils.getUserId(this).toString()
        val imagesRef = storageRef.child("/users/$userId/profile/photo_banner.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                bannerUrl = uri.toString()
                // Guardar la URL de la foto en la base de datos de Firebase Firestore
                saveBannerUrlToFirestore(bannerUrl)
            }.addOnFailureListener {
                showAlertToast("Error getting photo URL: ${it.message}")
            }
        }.addOnFailureListener { exception ->
            showAlertToast("Error uploading photo: ${exception.message}")
        }
    }

    private fun savePhotoUrlToFirestore(photoUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val userUpdate = hashMapOf<String, Any>("profileImage" to photoUrl)

        usersCollection.document(userId)
            .update(userUpdate)
            .addOnSuccessListener {
                //showAlertToast("Profile photo updated successfully.")
                newPhotoBitmap = null
            }
            .addOnFailureListener { e ->
                showAlertToast("Error updating profile photo URL: ${e.message}")
            }
    }

    private fun saveBannerUrlToFirestore(bannerUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val userUpdate = hashMapOf<String, Any>("bannerImage" to bannerUrl)

        usersCollection.document(userId)
            .update(userUpdate)
            .addOnSuccessListener {
                //showAlertToast("Profile photo updated successfully.")
                newBannerBitmap = null
            }
            .addOnFailureListener { e ->
                showAlertToast("Error updating profile photo URL: ${e.message}")
            }
    }

    private fun showPhotoOptionsDialog() {
        if (cameraOptionsDialog == null) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.camera_options, null)
            val takePhotoButton = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val choosePhotoButton = dialogView.findViewById<Button>(R.id.btnChoosePhoto)

            takePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                } else {
                    openCameraProfile()
                }
                cameraOptionsDialog?.dismiss()
            }

            choosePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PROFILE_GALLERY_REQUEST_CODE)
                } else {
                    openGalleryProfile()
                }
                cameraOptionsDialog?.dismiss()
            }

            cameraOptionsDialog = Dialog(this)
            cameraOptionsDialog?.setContentView(dialogView)
            cameraOptionsDialog?.setCancelable(true)
        }

        cameraOptionsDialog?.show()
    }

    private fun showBannerOptionsDialog() {
        if (cameraOptionsDialogBanner == null) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.camera_options, null)
            val takePhotoButton = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val choosePhotoButton = dialogView.findViewById<Button>(R.id.btnChoosePhoto)

            takePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE_BANNER)
                } else {
                    openCameraBanner()
                }
                cameraOptionsDialogBanner?.dismiss()
            }

            choosePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PROFILE_GALLERY_REQUEST_CODE_BANNER)
                } else {
                    openGalleryBanner()
                }
                cameraOptionsDialogBanner?.dismiss()
            }

            cameraOptionsDialogBanner = Dialog(this)
            cameraOptionsDialogBanner?.setContentView(dialogView)
            cameraOptionsDialogBanner?.setCancelable(true)
        }

        cameraOptionsDialogBanner?.show()
    }

    private fun openCameraProfile() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, PROFILE_CAMERA_REQUEST_CODE)
    }

    private fun openGalleryProfile() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PROFILE_GALLERY_REQUEST_CODE)
        } else {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PROFILE_GALLERY_REQUEST_CODE)
        }
    }

    private fun openCameraBanner() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, PROFILE_CAMERA_REQUEST_CODE_BANNER)
    }

    private fun openGalleryBanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PROFILE_GALLERY_REQUEST_CODE_BANNER)
        } else {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, PROFILE_GALLERY_REQUEST_CODE_BANNER)
        }
    }

    private fun deleteAccount() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Confirm Delete Account")
        alertDialogBuilder.setMessage("Are you sure you want to delete your account?")
        alertDialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            // Eliminar la cuenta y los datos asociados
            deleteUserData()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteUserData() {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        // Eliminar el documento del usuario del Cloud Firestore
        usersCollection.document(userId)
            .delete()
            .addOnSuccessListener {
                // Eliminar la cuenta de autenticación de Firebase
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()
                    ?.addOnSuccessListener {
                        showAlertToast("Account deleted successfully.")
                        // Eliminar datos en el almacenamiento de Firebase si existen
                        deleteStorageData()

                        val intent = Intent(this, LogIn::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    }
                    ?.addOnFailureListener { e ->
                        showAlertToast("Error deleting account: ${e.message}")
                    }
                    ?: run {//Esto ya deberia de funcionar porque todos se guardan en el authentication
                        // Si el usuario no está autenticado, solo eliminamos los datos asociados en Firestore y Storage
                        showAlertToast("Account deleted successfully.")
                        // Eliminar datos en el almacenamiento de Firebase si existen
                        deleteStorageData()

                        val intent = Intent(this, LogIn::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error deleting user data: ${e.message}")
            }
    }

    private fun deleteStorageData() {
        // Eliminar datos en el almacenamiento de Firebase si existen
        val storageRef = FirebaseStorage.getInstance().reference
        val userFolderRef = storageRef.child("users/$userId/")

        /*val profileImageRef = storageRef.child("users/$userId/profile/photo_profile.jpg")
        val bannerImageRef = storageRef.child("users/$userId/profile/photo_banner.jpg")

        profileImageRef.delete().addOnSuccessListener {
            // Foto de perfil eliminada correctamente
        }.addOnFailureListener { e ->
            // Error al eliminar la foto de perfil
            showAlertToast("Error deleting profile photo: ${e.message}")
        }

        bannerImageRef.delete().addOnSuccessListener {
            // Imagen del banner eliminada correctamente
        }.addOnFailureListener { e ->
            // Error al eliminar la imagen del banner
            showAlertToast("Error deleting banner image: ${e.message}")
        }

        // Eliminar otras fotos del usuario que estén almacenadas
        val otherPhotosRef = storageRef.child("users/$userId/")
        otherPhotosRef.listAll()
            .addOnSuccessListener { listResult ->
                for (fileRef in listResult.items) {
                    fileRef.delete().addOnSuccessListener {
                        // Foto eliminada correctamente
                    }.addOnFailureListener { e ->
                        showAlertToast("Error deleting photo: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error listing user photos: ${e.message}")
            } */

        userFolderRef.listAll()
            .addOnSuccessListener { listResult ->
                // Eliminar todos los archivos en la carpeta del usuario
                for (fileRef in listResult.items) {
                    fileRef.delete().addOnSuccessListener {
                        // Archivo eliminado correctamente
                    }.addOnFailureListener { e ->
                        showAlertToast("Error deleting file: ${e.message}")
                    }
                }

                // Recursivamente eliminar todos los subdirectorios y sus archivos
                for (prefix in listResult.prefixes) {
                    deleteDirectory(prefix)
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error listing user files: ${e.message}")
            }

        // Eliminar documentos en los que las fotos tienen como atributo el ID del usuario
        val db = FirebaseFirestore.getInstance()
        val photosCollection = db.collection("posts")
        photosCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    document.reference.delete().addOnSuccessListener {
                        // Documento de la foto eliminado correctamente
                    }.addOnFailureListener { e ->
                        showAlertToast("Error deleting photo document: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error listing photo documents: ${e.message}")
            }
    }

    private fun deleteDirectory(directoryRef: StorageReference) {
        directoryRef.listAll()
            .addOnSuccessListener { listResult ->
                for (fileRef in listResult.items) {
                    fileRef.delete().addOnSuccessListener {
                        // Archivo eliminado correctamente
                    }.addOnFailureListener { e ->
                        showAlertToast("Error deleting file: ${e.message}")
                    }
                }

                for (prefix in listResult.prefixes) {
                    deleteDirectory(prefix)
                }
            }
            .addOnFailureListener { e ->
                showAlertToast("Error listing directory files: ${e.message}")
            }
    }


    private fun showAlertDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Invalid credentials")
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PROFILE_CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // Aplicar máscara circular a la imagen
                    Glide.with(this)
                        .load(imageBitmap)
                        .transform(CircleCrop())
                        .into(binding.editUserCircle)

                    // Guardar la foto en una variable para guardarla si se presiona el botón de guardar
                    newPhotoBitmap = imageBitmap
                }
                PROFILE_GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    // Aplicar máscara circular a la imagen
                    Glide.with(this)
                        .load(imageBitmap)
                        .transform(CircleCrop())
                        .into(binding.editUserCircle)

                    // Guardar la foto en una variable para guardarla si se presiona el botón de guardar
                    newPhotoBitmap = imageBitmap
                }

                PROFILE_CAMERA_REQUEST_CODE_BANNER -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    // Aplicar máscara circular a la imagen
                    Glide.with(this)
                        .load(imageBitmap)
                        .into(binding.banner)

                    // Guardar la foto en una variable para guardarla si se presiona el botón de guardar
                    newBannerBitmap = imageBitmap
                }
                PROFILE_GALLERY_REQUEST_CODE_BANNER -> {
                    val imageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    // Aplicar máscara circular a la imagen
                    Glide.with(this)
                        .load(imageBitmap)
                        .into(binding.banner)

                    // Guardar la foto en una variable para guardarla si se presiona el botón de guardar
                    newBannerBitmap = imageBitmap
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraProfile()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PROFILE_GALLERY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryProfile()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == CAMERA_PERMISSION_CODE_BANNER) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraBanner()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == PROFILE_GALLERY_REQUEST_CODE_BANNER) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryBanner()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraOptionsDialog?.dismiss()
    }
}
