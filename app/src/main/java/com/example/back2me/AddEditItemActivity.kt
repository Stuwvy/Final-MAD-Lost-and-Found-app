package com.example.back2me

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.back2me.ItemRepository
import com.example.back2me.databinding.ActivityAddEditItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class AddEditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditItemBinding
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            displayImage(bitmap)
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imagePreview.setImageURI(uri)
            binding.imagePreview.visibility = View.VISIBLE
            binding.uploadPrompt.visibility = View.GONE
            binding.buttonRemovePhoto.visibility = View.VISIBLE
        }
    }

    // Permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setDefaultDateTime()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { finish() }
        binding.buttonCancel.setOnClickListener { finish() }
        binding.buttonSaveItem.setOnClickListener { postItem() }

        // Date & Time
        binding.inputDate.setOnClickListener { showDatePicker() }
        binding.inputTime.setOnClickListener { showTimePicker() }

        // Photo actions - Show dialog when clicking photo card
        binding.photoCard.setOnClickListener { showPhotoOptionsDialog() }
        binding.buttonRemovePhoto.setOnClickListener { removePhoto() }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose from Gallery")

        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen() // Camera
                    1 -> galleryLauncher.launch("image/*") // Gallery
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setDefaultDateTime() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        binding.inputDate.setText(dateFormat.format(selectedDate.time))
        binding.inputTime.setText(timeFormat.format(selectedDate.time))
    }

    private fun showDatePicker() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate.set(Calendar.YEAR, selectedYear)
            selectedDate.set(Calendar.MONTH, selectedMonth)
            selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.inputDate.setText(dateFormat.format(selectedDate.time))
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val hour = selectedDate.get(Calendar.HOUR_OF_DAY)
        val minute = selectedDate.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedDate.set(Calendar.MINUTE, selectedMinute)

            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.inputTime.setText(timeFormat.format(selectedDate.time))
        }, hour, minute, false).show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun displayImage(bitmap: Bitmap) {
        binding.imagePreview.setImageBitmap(bitmap)
        binding.imagePreview.visibility = View.VISIBLE
        binding.uploadPrompt.visibility = View.GONE
        binding.buttonRemovePhoto.visibility = View.VISIBLE

        // Convert bitmap to URI for upload
        selectedImageUri = getImageUriFromBitmap(bitmap)
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Item_Photo", null)
        return Uri.parse(path)
    }

    private fun removePhoto() {
        selectedImageUri = null
        uploadedImageUrl = ""
        binding.imagePreview.visibility = View.GONE
        binding.imagePreview.setImageDrawable(null)
        binding.uploadPrompt.visibility = View.VISIBLE
        binding.buttonRemovePhoto.visibility = View.GONE
    }

    private fun postItem() {
        val name = binding.inputItemName.text.toString().trim()
        val location = binding.inputLocation.text.toString().trim()
        val description = binding.inputDescription.text.toString().trim()
        val status = if (binding.radioLost.isChecked) "lost" else "found"

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Item Name and Location are required.", Toast.LENGTH_LONG).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

        // Convert selected date to ISO format
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val createdDate = isoFormat.format(selectedDate.time)

        binding.buttonSaveItem.isEnabled = false
        binding.buttonSaveItem.text = "Posting..."

        lifecycleScope.launch {
            // Upload image if selected
            if (selectedImageUri != null) {
                uploadedImageUrl = uploadImageToFirebase(selectedImageUri!!) ?: ""
            }

            val newItem = Item(
                name = name,
                location = location,
                description = description,
                status = status,
                createdBy = userId,
                createdDate = createdDate,
                imageUrl = uploadedImageUrl
            )

            val result = ItemRepository.createItem(newItem)

            binding.buttonSaveItem.isEnabled = true
            binding.buttonSaveItem.text = "Submit"

            if (result.isSuccess) {
                Toast.makeText(this@AddEditItemActivity, "Item posted successfully!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this@AddEditItemActivity, "Failed to post item: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun uploadImageToFirebase(imageUri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("item_images/${UUID.randomUUID()}.jpg")

            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}