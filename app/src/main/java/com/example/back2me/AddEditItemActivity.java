package com.example.back2me;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.back2me.databinding.ActivityAddEditItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class AddEditItemActivity extends AppCompatActivity {

    private ActivityAddEditItemBinding binding;
    private Calendar selectedDate = Calendar.getInstance();
    private Uri selectedImageUri = null;
    private String uploadedImageUrl = "";

    // Camera launcher
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    displayImage(bitmap);
                }
            });

    // Gallery launcher
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    binding.imagePreview.setImageURI(uri);
                    binding.imagePreview.setVisibility(View.VISIBLE);
                    binding.uploadPrompt.setVisibility(View.GONE);
                    binding.buttonRemovePhoto.setVisibility(View.VISIBLE);
                }
            });

    // Permission launcher
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupClickListeners();
        setDefaultDateTime();
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.buttonCancel.setOnClickListener(v -> finish());
        binding.buttonSaveItem.setOnClickListener(v -> postItem());

        // Date & Time
        binding.inputDate.setOnClickListener(v -> showDatePicker());
        binding.inputTime.setOnClickListener(v -> showTimePicker());

        // Photo actions - Show dialog when clicking photo card
        binding.photoCard.setOnClickListener(v -> showPhotoOptionsDialog());
        binding.buttonRemovePhoto.setOnClickListener(v -> removePhoto());
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"📷 Take Photo", "🖼️ Choose from Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen(); // Camera
                    } else {
                        galleryLauncher.launch("image/*"); // Gallery
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setDefaultDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        binding.inputDate.setText(dateFormat.format(selectedDate.getTime()));
        binding.inputTime.setText(timeFormat.format(selectedDate.getTime()));
    }

    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedDate.set(Calendar.YEAR, selectedYear);
            selectedDate.set(Calendar.MONTH, selectedMonth);
            selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.inputDate.setText(dateFormat.format(selectedDate.getTime()));
        }, year, month, day).show();
    }

    private void showTimePicker() {
        int hour = selectedDate.get(Calendar.HOUR_OF_DAY);
        int minute = selectedDate.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedDate.set(Calendar.MINUTE, selectedMinute);

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            binding.inputTime.setText(timeFormat.format(selectedDate.getTime()));
        }, hour, minute, false).show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        cameraLauncher.launch(null);
    }

    private void displayImage(Bitmap bitmap) {
        binding.imagePreview.setImageBitmap(bitmap);
        binding.imagePreview.setVisibility(View.VISIBLE);
        binding.uploadPrompt.setVisibility(View.GONE);
        binding.buttonRemovePhoto.setVisibility(View.VISIBLE);

        // Convert bitmap to URI for upload
        selectedImageUri = getImageUriFromBitmap(bitmap);
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Item_Photo", null);
        return Uri.parse(path);
    }

    private void removePhoto() {
        selectedImageUri = null;
        uploadedImageUrl = "";
        binding.imagePreview.setVisibility(View.GONE);
        binding.imagePreview.setImageDrawable(null);
        binding.uploadPrompt.setVisibility(View.VISIBLE);
        binding.buttonRemovePhoto.setVisibility(View.GONE);
    }

    private void postItem() {
        String name = binding.inputItemName.getText().toString().trim();
        String location = binding.inputLocation.getText().toString().trim();
        String description = binding.inputDescription.getText().toString().trim();
        String status = binding.radioLost.isChecked() ? "lost" : "found";

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Item Name and Location are required.", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";

        // Convert selected date to ISO format
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String createdDate = isoFormat.format(selectedDate.getTime());

        binding.buttonSaveItem.setEnabled(false);
        binding.buttonSaveItem.setText("Posting...");

        // Upload image if selected, then create item
        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, imageUrl -> {
                uploadedImageUrl = imageUrl != null ? imageUrl : "";
                createItemInFirestore(name, location, description, status, userId, createdDate);
            });
        } else {
            createItemInFirestore(name, location, description, status, userId, createdDate);
        }
    }

    private void createItemInFirestore(String name, String location, String description,
                                       String status, String userId, String createdDate) {
        Item newItem = new Item(name, location, description, status, userId, createdDate, uploadedImageUrl);

        ItemRepository.createItem(newItem, new ItemRepository.CreateCallback() {
            @Override
            public void onSuccess(Item item) {
                binding.buttonSaveItem.setEnabled(true);
                binding.buttonSaveItem.setText("Submit");

                Toast.makeText(AddEditItemActivity.this, "Item posted successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.buttonSaveItem.setEnabled(true);
                binding.buttonSaveItem.setText("Submit");

                Toast.makeText(AddEditItemActivity.this,
                        "Failed to post item: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri, OnImageUploadListener listener) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("item_images/" + UUID.randomUUID().toString() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> listener.onUploadComplete(uri.toString()))
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                listener.onUploadComplete(null);
                            });
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    listener.onUploadComplete(null);
                });
    }

    // Interface for image upload callback
    private interface OnImageUploadListener {
        void onUploadComplete(String imageUrl);
    }
}