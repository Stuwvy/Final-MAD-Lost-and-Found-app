package com.example.back2me;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.back2me.databinding.ActivityEditItemBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class EditItemActivity extends AppCompatActivity {

    private static final String TAG = "EditItemActivity";

    private ActivityEditItemBinding binding;
    private String itemId;
    private Item currentItem;
    private Calendar selectedDate = Calendar.getInstance();
    private Uri selectedImageUri = null;
    private String currentImageUrl = "";
    private boolean imageChanged = false;

    // Camera launcher
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    displayImage(bitmap);
                    imageChanged = true;
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
                    imageChanged = true;
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
        binding = ActivityEditItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        itemId = getIntent().getStringExtra("ITEM_ID");

        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Item ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
        loadItemData();
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());
        binding.buttonCancel.setOnClickListener(v -> finish());
        binding.buttonSaveItem.setOnClickListener(v -> saveChanges());

        // Date & Time
        binding.inputDate.setOnClickListener(v -> showDatePicker());
        binding.inputTime.setOnClickListener(v -> showTimePicker());

        // Photo actions
        binding.photoCard.setOnClickListener(v -> showPhotoOptionsDialog());
        binding.buttonRemovePhoto.setOnClickListener(v -> removePhoto());
    }

    private void loadItemData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ItemRepository.getItemById(itemId, new ItemRepository.ItemCallback() {
            @Override
            public void onSuccess(Item item) {
                binding.progressBar.setVisibility(View.GONE);

                if (item != null) {
                    currentItem = item;
                    populateFields(item);
                } else {
                    Toast.makeText(EditItemActivity.this, "Item not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading item", e);
                Toast.makeText(EditItemActivity.this, "Error loading item", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(Item item) {
        // Set item name
        binding.inputItemName.setText(item.getName());

        // Set location
        binding.inputLocation.setText(item.getLocation());

        // Set description
        binding.inputDescription.setText(item.getDescription());

        // Set status radio button
        if ("found".equals(item.getStatus().toLowerCase())) {
            binding.radioFound.setChecked(true);
        } else {
            binding.radioLost.setChecked(true);
        }

        // Parse and set date/time
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = isoFormat.parse(item.getCreatedDate());

            if (date != null) {
                selectedDate.setTime(date);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                binding.inputDate.setText(dateFormat.format(date));
                binding.inputTime.setText(timeFormat.format(date));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error", e);
        }

        // Load existing image
        currentImageUrl = item.getImageUrl();
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentImageUrl)
                    .centerCrop()
                    .into(binding.imagePreview);
            binding.imagePreview.setVisibility(View.VISIBLE);
            binding.uploadPrompt.setVisibility(View.GONE);
            binding.buttonRemovePhoto.setVisibility(View.VISIBLE);
        }
    }

    private void showPhotoOptionsDialog() {
        String[] options = {"📷 Take Photo", "🖼️ Choose from Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Change Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        currentImageUrl = "";
        imageChanged = true;
        binding.imagePreview.setVisibility(View.GONE);
        binding.imagePreview.setImageDrawable(null);
        binding.uploadPrompt.setVisibility(View.VISIBLE);
        binding.buttonRemovePhoto.setVisibility(View.GONE);
    }

    private void saveChanges() {
        String name = binding.inputItemName.getText().toString().trim();
        String location = binding.inputLocation.getText().toString().trim();
        String description = binding.inputDescription.getText().toString().trim();
        String status = binding.radioLost.isChecked() ? "lost" : "found";

        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Item Name and Location are required.", Toast.LENGTH_LONG).show();
            return;
        }

        binding.buttonSaveItem.setEnabled(false);
        binding.buttonSaveItem.setText("Saving...");

        // Convert selected date to ISO format
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String createdDate = isoFormat.format(selectedDate.getTime());

        if (imageChanged && selectedImageUri != null) {
            // Upload new image first
            uploadImageToFirebase(selectedImageUri, imageUrl -> {
                String finalImageUrl = imageUrl != null ? imageUrl : "";
                updateItemInFirestore(name, location, description, status, createdDate, finalImageUrl);
            });
        } else {
            // No image change, use existing URL
            String imageUrl = imageChanged ? "" : currentImageUrl; // If removed, use empty string
            updateItemInFirestore(name, location, description, status, createdDate, imageUrl);
        }
    }

    private void updateItemInFirestore(String name, String location, String description,
                                       String status, String createdDate, String imageUrl) {
        Item updatedItem = new Item(
                itemId,
                name,
                location,
                description,
                status,
                currentItem.getCreatedBy(),
                createdDate,
                imageUrl
        );

        ItemRepository.updateItem(itemId, updatedItem, new ItemRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                binding.buttonSaveItem.setEnabled(true);
                binding.buttonSaveItem.setText("Save Changes");

                Toast.makeText(EditItemActivity.this, "Item updated successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                binding.buttonSaveItem.setEnabled(true);
                binding.buttonSaveItem.setText("Save Changes");

                Toast.makeText(EditItemActivity.this,
                        "Failed to update item: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private interface OnImageUploadListener {
        void onUploadComplete(String imageUrl);
    }
}