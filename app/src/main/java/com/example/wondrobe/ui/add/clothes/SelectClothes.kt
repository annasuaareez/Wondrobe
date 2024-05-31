package com.example.wondrobe.ui.add.clothes

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.wondrobe.R
import com.example.wondrobe.ui.add.post.SelectPost
import java.io.ByteArrayOutputStream

class SelectClothes : AppCompatActivity() {
    companion object {
        const val NUM_COLUMNS = 4
        const val MAX_IMAGE_COUNT = 60
        const val IMAGE_LOAD_ERROR = "Error loading images"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_GALLERY = 123
        private const val REQUEST_CAMERA_PERMISSION = 100
    }

    private lateinit var imageGrid: GridLayout
    private lateinit var loadingProgressBar: ProgressBar
    private var selectedImageUri: Uri? = null

    @SuppressLint("IntentReset")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_clothes)

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        val closeIcon = findViewById<ImageView>(R.id.closeIconSelectClothes)
        val nextButton = findViewById<AppCompatButton>(R.id.nextButtonClothes)
        val cameraIcon = findViewById<ImageView>(R.id.cameraIconClothes)
        val galleryIcon = findViewById<ImageView>(R.id.galleryIconClothes)

        imageGrid = findViewById(R.id.imageGridClothes)
        loadingProgressBar = findViewById(R.id.loadingProgressBarClothes)

        loadImagesFromMediaStore()
        setupCloseIcon(closeIcon)
        //loadImages(contentResolver, imageGrid, loadingProgressBar).execute()

        nextButton.isEnabled = false
        nextButton.setBackgroundResource(R.drawable.button_gray)

        nextButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                // Obtener la URL absoluta de la imagen seleccionada
                val absolutePath = uri.path ?: ""
                val absoluteUri = Uri.parse("file://$absolutePath")
                Log.e("SelectedImageUri", absoluteUri.toString())

                val intent = Intent(this, AddClothes::class.java)
                intent.putExtra("imageUri", absoluteUri.toString())
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }

        cameraIcon.setOnClickListener {
            checkCameraPermission()
        }

        galleryIcon.setOnClickListener {
            openGallery()
        }

    }

    override fun onResume() {
        super.onResume()
        loadImagesFromMediaStore()
    }

    private fun setupCloseIcon(closeIcon: ImageView) {
        closeIcon.setOnClickListener {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val uri = getImageUri(imageBitmap)
            if (uri != null) {
                openAddClothesActivity(uri)
            } else {
                Toast.makeText(this, "Error al obtener la URI de la imagen", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                val absolutePath = getRealPathFromUri(imageUri)
                if (absolutePath != null) {
                    openAddClothesActivity(Uri.parse("file://$absolutePath"))
                } else {
                    Toast.makeText(this, "Error al obtener la ruta absoluta de la imagen", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }

    private fun openAddClothesActivity(imageUri: Uri) {
        val intent = Intent(this, AddClothes::class.java)
        intent.putExtra("imageUri", imageUri.toString())
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            openCamera()
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_GALLERY
            )
        } else {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhotoIntent, REQUEST_GALLERY)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == REQUEST_GALLERY) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class loadImages(
        private val contentResolver: ContentResolver,
        private val imageGrid: GridLayout,
        private val loadingProgressBar: ProgressBar
    ) : AsyncTask<Void, Void, List<Bitmap>>() {

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            loadingProgressBar.visibility = View.VISIBLE
        }

        @Deprecated("Deprecated in Java")
        @SuppressLint("Range")
        override fun doInBackground(vararg params: Void?): List<Bitmap> {
            val images = mutableListOf<Bitmap>()
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

            cursor?.use {
                while (it.moveToNext() && images.size < MAX_IMAGE_COUNT) {
                    val imageId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId.toString())
                    val bitmap = decodeSampledBitmapFromUri(imageUri, 200, 200)
                    if (bitmap != null) {
                        images.add(bitmap)
                    }
                }
            }

            return images
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: List<Bitmap>?) {
            super.onPostExecute(result)
            loadingProgressBar.visibility = View.GONE
            result?.let {
                setupGridLayout(imageGrid, it)
            } ?: showError(IMAGE_LOAD_ERROR)
        }
    }

    private fun decodeSampledBitmapFromUri(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun setupGridLayout(imageGrid: GridLayout, images: List<Bitmap>) {
        imageGrid.apply {
            rowCount = (images.size + NUM_COLUMNS - 1) / NUM_COLUMNS
            removeAllViews()
        }

        val imageWidth = resources.getDimensionPixelSize(R.dimen.image_width)
        val imageHeight = resources.getDimensionPixelSize(R.dimen.image_height)
        val margin = resources.getDimensionPixelSize(R.dimen.image_margin)

        images.forEachIndexed { index, bitmap ->
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(index / NUM_COLUMNS, 1f),
                GridLayout.spec(index % NUM_COLUMNS, 1f)
            ).apply {
                width = imageWidth
                height = imageHeight
                setMargins(margin, margin, margin, margin)
            }

            imageView.setOnClickListener {
                handleImageClick(bitmap, imageView)
            }

            imageGrid.addView(imageView)
        }
    }

    private fun handleImageClick(bitmap: Bitmap, imageView: ImageView) {
        selectedImageUri = getImageUri(bitmap)
        resetImageGrid(imageGrid)
        applyGrayScaleToOthers(imageGrid, imageView)
        imageView.isClickable = true
        findViewById<AppCompatButton>(R.id.nextButtonClothes).isEnabled = true
        findViewById<AppCompatButton>(R.id.nextButtonClothes).setBackgroundResource(R.drawable.button_purple)
    }
    private fun resetImageGrid(imageGrid: GridLayout) {
        imageGrid.children.forEach { child ->
            if (child is ImageView) {
                applyNormalColor(child)
                child.isClickable = true
            }
        }
    }

    private fun applyGrayScaleToOthers(imageGrid: GridLayout, selectedImageView: ImageView) {
        imageGrid.children.forEach { child ->
            if (child is ImageView && child != selectedImageView) {
                applyGrayScale(child)
                child.isClickable = true
            }
        }
    }

    private fun applyGrayScale(imageView: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(matrix)
        imageView.colorFilter = filter
    }

    private fun applyNormalColor(imageView: ImageView) {
        imageView.colorFilter = null
    }

    private fun loadImagesFromMediaStore() {
        loadImages(contentResolver, imageGrid, loadingProgressBar).execute()
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}