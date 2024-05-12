package com.example.wondrobe.ui.add.post

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.example.wondrobe.R
import java.io.ByteArrayOutputStream

class SelectPost : AppCompatActivity() {
    private companion object {
        const val NUM_COLUMNS = 4
        const val MAX_IMAGE_COUNT = 60
        const val IMAGE_LOAD_ERROR = "Error loading images"
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_GALLERY = 123
    }

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_post)

        val closeIcon = findViewById<ImageView>(R.id.closeIconSelect)
        val imageGrid = findViewById<GridLayout>(R.id.imageGrid)
        val loadingProgressBar = findViewById<ProgressBar>(R.id.loadingProgressBar)
        val contentResolver: ContentResolver = contentResolver
        val nextButton = findViewById<AppCompatButton>(R.id.nextButton)
        val cameraIcon = findViewById<ImageView>(R.id.cameraIcon)
        val galleryIcon = findViewById<ImageView>(R.id.galleryIcon)

        setupCloseIcon(closeIcon)
        loadImages(contentResolver, imageGrid, loadingProgressBar).execute()

        nextButton.isEnabled = false
        nextButton.setBackgroundResource(R.drawable.button_gray)

        nextButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                Log.i("SelectedImageUri", uri.toString())
                val intent = Intent(this, AddPost::class.java)
                intent.putExtra("imageUri", uri.toString())
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        cameraIcon.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "No se puede abrir la cámara", Toast.LENGTH_SHORT).show()
            }
        }

        galleryIcon.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickPhotoIntent.type = "image/*"
            startActivityForResult(pickPhotoIntent, REQUEST_GALLERY)
        }
    }

    private fun setupCloseIcon(closeIcon: ImageView) {
        closeIcon.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val uri = getImageUri(imageBitmap)
            if (uri != null) {
                openAddPostActivity(uri)
            } else {
                Toast.makeText(this, "Error al obtener la URI de la imagen", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                openAddPostActivity(imageUri)
            } else {
                Toast.makeText(this, "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openAddPostActivity(imageUri: Uri) {
        val intent = Intent(this, AddPost::class.java)
        intent.putExtra("imageUri", imageUri.toString())
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }


    private fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    /*@SuppressLint("Range")
    private fun loadImages(contentResolver: ContentResolver, imageGrid: GridLayout, loadingProgressBar: ProgressBar) {
        loadingProgressBar.visibility = View.VISIBLE

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

        if (cursor == null) {
            showError(IMAGE_LOAD_ERROR)
            return
        }

        try {
            val images = mutableListOf<Uri>()
            var rowIndex = 0
            var columnIndex = 0
            var totalImagesLoaded = 0
            while (cursor.moveToNext() && images.size < MAX_IMAGE_COUNT && totalImagesLoaded < Integer.MAX_VALUE) {
                val imageUri: Uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                images.add(imageUri)

                columnIndex++
                if (columnIndex % NUM_COLUMNS == 0) {
                    columnIndex = 0
                    rowIndex++
                }

                totalImagesLoaded++
                if (totalImagesLoaded % 12 == 0) {
                    // Load 12 more images when the user reaches the end of the current images
                    val remainingImagesToLoad = MAX_IMAGE_COUNT - images.size
                    val numImagesToLoad = minOf(12, remainingImagesToLoad)
                    if (numImagesToLoad == 0) {
                        break
                    }
                }
            }

            // Calculate the number of rows needed based on the number of images and the number of columns
            val rowCount = (images.size + NUM_COLUMNS - 1) / NUM_COLUMNS

            setupGridLayout(imageGrid, images, rowCount)
        } catch (e: Exception) {
            showError(e.message ?: IMAGE_LOAD_ERROR)
        } finally {
            loadingProgressBar.visibility = View.GONE
        }
    }

    private fun setupGridLayout(imageGrid: GridLayout, images: List<Uri>, rowCount: Int) {
        imageGrid.rowCount = rowCount
        val imageWidth = resources.getDimensionPixelSize(R.dimen.image_width)
        val imageHeight = resources.getDimensionPixelSize(R.dimen.image_height)
        val margin = resources.getDimensionPixelSize(R.dimen.image_margin)

        images.forEachIndexed { index, uri ->
            val imageView = ImageView(this)
            imageView.setImageURI(uri)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(index / NUM_COLUMNS, 1f),
                GridLayout.spec(index % NUM_COLUMNS, 1f)
            ).apply {
                width = imageWidth
                height = imageHeight
                setMargins(margin, margin, margin, margin)
            }

            imageGrid.addView(imageView)
        }
    }

    @SuppressLint("Range")
    private fun loadImages(contentResolver: ContentResolver, imageGrid: GridLayout, loadingProgressBar: ProgressBar) {
        loadingProgressBar.visibility = View.VISIBLE

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

        if (cursor == null) {
            showError(IMAGE_LOAD_ERROR)
            return
        }

        try {
            val images = mutableListOf<Uri>()
            while (cursor.moveToNext()) {
                val imageUri: Uri = Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                images.add(imageUri)
            }

            setupGridLayout(imageGrid, images)
        } catch (e: Exception) {
            showError(e.message ?: IMAGE_LOAD_ERROR)
        } finally {
            loadingProgressBar.visibility = View.GONE
        }
    }*/

    private inner class loadImages(
        private val contentResolver: ContentResolver,
        private val imageGrid: GridLayout,
        private val loadingProgressBar: ProgressBar
    ) : AsyncTask<Void, Void, List<Uri>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            loadingProgressBar.visibility = View.VISIBLE
        }

        @SuppressLint("Range")
        override fun doInBackground(vararg params: Void?): List<Uri> {
            val images = mutableListOf<Uri>()
            val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
            val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

            cursor?.use {
                while (it.moveToNext() && images.size < MAX_IMAGE_COUNT) {
                    val imageUri: Uri = Uri.parse(it.getString(it.getColumnIndex(MediaStore.Images.Media.DATA)))
                    images.add(imageUri)
                }
            }

            return images
        }

        override fun onPostExecute(result: List<Uri>?) {
            super.onPostExecute(result)
            loadingProgressBar.visibility = View.GONE
            result?.let {
                setupGridLayout(imageGrid, it)
            } ?: showError(IMAGE_LOAD_ERROR)
        }
    }

    private fun setupGridLayout(imageGrid: GridLayout, images: List<Uri>) {
        imageGrid.rowCount = (images.size + NUM_COLUMNS - 1) / NUM_COLUMNS
        val imageWidth = resources.getDimensionPixelSize(R.dimen.image_width)
        val imageHeight = resources.getDimensionPixelSize(R.dimen.image_height)
        val margin = resources.getDimensionPixelSize(R.dimen.image_margin)

        images.forEachIndexed { index, uri ->
            val imageView = ImageView(this)
            imageView.setImageURI(uri)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(index / NUM_COLUMNS, 1f),
                GridLayout.spec(index % NUM_COLUMNS, 1f)
            ).apply {
                width = imageWidth
                height = imageHeight
                setMargins(margin, margin, margin, margin)
            }

            // Clic listener para cada imagen
            imageView.setOnClickListener {
                if (selectedImageUri == uri) {
                    // Si se hace clic en la imagen seleccionada nuevamente, restaurar todas las imágenes a su estado normal
                    selectedImageUri = null
                    resetImageGrid(imageGrid)
                    findViewById<AppCompatButton>(R.id.nextButton).isEnabled = false
                    findViewById<AppCompatButton>(R.id.nextButton).setBackgroundResource(R.drawable.button_gray)
                } else {
                    // Almacenar la URI de la imagen seleccionada
                    selectedImageUri = uri

                    // Desactivar todas las imágenes
                    resetImageGrid(imageGrid)

                    // Activar la imagen seleccionada
                    applyGrayScaleToOthers(imageGrid, imageView)
                    imageView.isClickable = true // Permitir clic

                    // Activar el botón "Next"
                    findViewById<AppCompatButton>(R.id.nextButton).isEnabled = true
                    findViewById<AppCompatButton>(R.id.nextButton).setBackgroundResource(R.drawable.button_purple)
                }
            }

            imageGrid.addView(imageView)
        }
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

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}