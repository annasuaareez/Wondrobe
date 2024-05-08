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
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wondrobe.R
import com.example.wondrobe.databinding.FragmentAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentAddBinding? = null

    private val CAMERA_PERMISSION_CODE = 100
    private val PROFILE_CAMERA_REQUEST_CODE = 101
    private val PROFILE_GALLERY_REQUEST_CODE = 102

    private val CAMERA_PERMISSION_CODE_BANNER = 103
    private val PROFILE_CAMERA_REQUEST_CODE_BANNER = 104
    private val PROFILE_GALLERY_REQUEST_CODE_BANNER = 105

    private var cameraOptionsDialog: Dialog? = null
    private var newPhotoBitmap: Bitmap? = null
    private var newBannerBitmap: Bitmap? = null
    private var photoNumber = 0

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
            showPhotoOptionsDialog()
        }

        return root
    }

    private fun showPhotoOptionsDialog() {
        if (cameraOptionsDialog == null) {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.camera_options, null)
            val takePhotoButton = dialogView.findViewById<Button>(R.id.btnTakePhoto)
            val choosePhotoButton = dialogView.findViewById<Button>(R.id.btnChoosePhoto)

            takePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                } else {
                    openCameraProfile()
                }
                cameraOptionsDialog?.dismiss()
            }

            choosePhotoButton.setOnClickListener {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PROFILE_GALLERY_REQUEST_CODE)
                } else {
                    openGalleryProfile()
                }
                cameraOptionsDialog?.dismiss()
            }

            cameraOptionsDialog = Dialog(requireContext())
            cameraOptionsDialog?.setContentView(dialogView)
            cameraOptionsDialog?.setCancelable(true)
        }

        cameraOptionsDialog?.show()
    }

    private fun openCameraProfile() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, PROFILE_CAMERA_REQUEST_CODE)
    }

    private fun openGalleryProfile() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, PROFILE_GALLERY_REQUEST_CODE)
    }

    private fun openCameraBanner() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, PROFILE_CAMERA_REQUEST_CODE_BANNER)
    }

    private fun openGalleryBanner() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, PROFILE_GALLERY_REQUEST_CODE_BANNER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PROFILE_CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    newPhotoBitmap = imageBitmap
                    savePhotoToFirebaseStorage(newPhotoBitmap!!)
                }
                PROFILE_GALLERY_REQUEST_CODE -> {
                    val imageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                    newPhotoBitmap = imageBitmap
                    savePhotoToFirebaseStorage(newPhotoBitmap!!)
                }
                PROFILE_CAMERA_REQUEST_CODE_BANNER -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    newBannerBitmap = imageBitmap
                    savePhotoToFirebaseStorage(newBannerBitmap!!)
                }
                PROFILE_GALLERY_REQUEST_CODE_BANNER -> {
                    val imageUri = data?.data
                    val imageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                    newBannerBitmap = imageBitmap
                    savePhotoToFirebaseStorage(newBannerBitmap!!)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCameraProfile()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            PROFILE_GALLERY_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryProfile()
                } else {
                    Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_CODE_BANNER -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCameraBanner()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            PROFILE_GALLERY_REQUEST_CODE_BANNER -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryBanner()
                } else {
                    Toast.makeText(requireContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    private fun savePhotoToFirebaseStorage(bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Obtiene el ID del usuario actual

        // Genera un nombre único para la imagen de acuerdo al número de fotos ya existentes
        val imageName = "post${getNextImageNumber()}"

        // Ruta de la imagen en Firebase Storage
        val imagesRef = storageRef.child("users/$userId/post/$imageName.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                // Guarda la URL de la foto en la base de datos Firestore
                savePhotoUrlToFirestore(photoUrl)
            }.addOnFailureListener {
                showAlertToast("Error obteniendo URL de la foto: ${it.message}")
            }
        }.addOnFailureListener { exception ->
            showAlertToast("Error al subir la foto: ${exception.message}")
        }
    }

    // Función para obtener el número siguiente para el nombre de la imagen
    private fun getNextImageNumber(): Int {
        val nextNumber = photoNumber
        photoNumber++
        return nextNumber
    }

    private fun savePhotoUrlToFirestore(photoUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Obtiene el ID del usuario actual
    }

    private fun showAlertToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
