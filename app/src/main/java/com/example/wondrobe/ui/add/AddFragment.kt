package com.example.wondrobe.ui.add

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.wondrobe.databinding.FragmentAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddBinding? = null

    private val PROFILE_GALLERY_REQUEST_CODE = 102

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.closeAddFragment.setOnClickListener {
            dismiss() // Cierra el fragmento
        }

        isCancelable = false

        binding.postButton.setOnClickListener {
            openGallery()
        }

        return root
    }

    private fun openGallery() {
        // Verificar permisos de almacenamiento externo
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permiso si no está concedido
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PROFILE_GALLERY_REQUEST_CODE
            )
        } else {
            // Si el permiso ya está concedido, abrir la galería
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PROFILE_GALLERY_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PROFILE_GALLERY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, abrir la galería
                openGallery()
            } else {
                showAlertToast("Permission Denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            // Obtener la imagen seleccionada de la galería
            val selectedImageUri = data.data
            selectedImageUri?.let {
                // Convertir la imagen URI en Bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                // Guardar la imagen en Firebase Storage
                savePhotoToFirebaseStorage(bitmap)
            }
        }
    }

    private fun savePhotoToFirebaseStorage(bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Obtener un nombre de archivo único para la imagen
        val imageName = "${System.currentTimeMillis()}.jpg"

        // Ruta de almacenamiento en Firebase
        val imagesRef = storageRef.child("/posts/$userId/$imageName")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                // Aquí puedes hacer lo que necesites con la URL de la imagen
                // Por ejemplo, guardar la URL en la base de datos de Firebase Firestore
                savePhotoUrlToFirestore(photoUrl)
            }.addOnFailureListener {
                showAlertToast("Error getting photo URL: ${it.message}")
            }
        }.addOnFailureListener { exception ->
            showAlertToast("Error uploading photo: ${exception.message}")
        }
    }

    private fun savePhotoUrlToFirestore(photoUrl: String) {
        // Aquí puedes implementar el código para guardar la URL de la imagen en Firestore
        // Por ejemplo, puedes obtener una referencia a la colección "posts" y agregar un nuevo documento con la URL de la imagen
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }
}
