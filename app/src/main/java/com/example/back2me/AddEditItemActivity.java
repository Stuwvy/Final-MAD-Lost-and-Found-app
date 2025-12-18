package com.example.back2me;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.back2me.databinding.ActivityAddEditItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddEditItemActivity extends AppCompatActivity {

    private static final String TAG = "AddEditItemActivity";

    private ActivityAddEditItemBinding binding;
    private FirebaseAuth auth;
    private Handler mainHandler;

    private String selectedStatus = "lost";
    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;
    private boolean isUploading = false;

    // Camera launcher
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    selectedImageUri = null;
                    displaySelectedImage(bitmap);
                    Log.d(TAG, "Camera image captured");
                }
            });

    // Gallery launcher
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedBitmap = null;
                    displaySelectedImage(uri);
                    Log.d(TAG, "Gallery image selected: " + uri);
                }
            });

    // Camera permission launcher
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraLauncher.launch(null);
                } else {
                    Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        mainHandler = new Handler(Looper.getMainLooper());

        setupUI();
        setupClickListeners();
    }

    private void setupUI() {
        binding.chipLost.setChecked(true);
        selectedStatus = "lost";
        binding.cardImagePreview.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.chipLost.setOnClickListener(v -> {
            selectedStatus = "lost";
            binding.chipLost.setChecked(true);
            binding.chipFound.setChecked(false);
        });

        binding.chipFound.setOnClickListener(v -> {
            selectedStatus = "found";
            binding.chipFound.setChecked(true);
            binding.chipLost.setChecked(false);
        });

        binding.buttonAddPhoto.setOnClickListener(v -> showImagePickerDialog());
        binding.buttonRemovePhoto.setOnClickListener(v -> removeSelectedImage());
        binding.buttonSubmit.setOnClickListener(v -> validateAndSubmit());
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndLaunch();
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null);
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void displaySelectedImage(Bitmap bitmap) {
        binding.cardImagePreview.setVisibility(View.VISIBLE);
        binding.imagePreview.setImageBitmap(bitmap);
        binding.buttonAddPhoto.setText(R.string.change_photo);
    }

    private void displaySelectedImage(Uri uri) {
        binding.cardImagePreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.imagePreview);
        binding.buttonAddPhoto.setText(R.string.change_photo);
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        selectedBitmap = null;
        binding.cardImagePreview.setVisibility(View.GONE);
        binding.imagePreview.setImageDrawable(null);
        binding.buttonAddPhoto.setText(R.string.add_photo);
    }

    private void validateAndSubmit() {
        String name = binding.editItemName.getText().toString().trim();
        String location = binding.editLocation.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();

        if (name.isEmpty()) {
            binding.editItemName.setError(getString(R.string.error_item_name_required));
            binding.editItemName.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            binding.editLocation.setError(getString(R.string.error_location_required));
            binding.editLocation.requestFocus();
            return;
        }

        if (isUploading) {
            Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_login, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Check if we have an image to upload
        if (selectedBitmap != null) {
            uploadBitmapToCloudinary(name, location, description, currentUser.getUid());
        } else if (selectedImageUri != null) {
            uploadUriToCloudinary(name, location, description, currentUser.getUid());
        } else {
            createItem(name, location, description, currentUser.getUid(), "");
        }
    }

    private void uploadBitmapToCloudinary(String name, String location, String description, String userId) {
        isUploading = true;
        Log.d(TAG, "Uploading bitmap to Cloudinary...");

        CloudinaryHelper.uploadBitmap(selectedBitmap, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                mainHandler.post(() -> {
                    isUploading = false;
                    Log.d(TAG, "Cloudinary upload success: " + imageUrl);
                    createItem(name, location, description, userId, imageUrl);
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    isUploading = false;
                    setLoading(false);
                    Log.e(TAG, "Cloudinary upload error: " + errorMessage);
                    Toast.makeText(AddEditItemActivity.this,
                            "Failed to upload image: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void uploadUriToCloudinary(String name, String location, String description, String userId) {
        isUploading = true;
        Log.d(TAG, "Uploading URI to Cloudinary...");

        CloudinaryHelper.uploadUri(this, selectedImageUri, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                mainHandler.post(() -> {
                    isUploading = false;
                    Log.d(TAG, "Cloudinary upload success: " + imageUrl);
                    createItem(name, location, description, userId, imageUrl);
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    isUploading = false;
                    setLoading(false);
                    Log.e(TAG, "Cloudinary upload error: " + errorMessage);
                    Toast.makeText(AddEditItemActivity.this,
                            "Failed to upload image: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void createItem(String name, String location, String description,
                            String userId, String imageUrl) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String createdDate = isoFormat.format(new Date());

        Item item = new Item(
                "",
                name,
                location,
                description,
                selectedStatus,
                userId,
                createdDate,
                imageUrl
        );

        Log.d(TAG, "Creating item with imageUrl: " + (imageUrl.isEmpty() ? "(none)" : imageUrl));

        ItemRepository.createItem(item, new ItemRepository.CreateCallback() {
            @Override
            public void onSuccess(Item createdItem) {
                setLoading(false);
                Log.d(TAG, "Item created successfully: " + createdItem.getId());
                Toast.makeText(AddEditItemActivity.this,
                        R.string.item_posted_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Log.e(TAG, "Error creating item", e);
                Toast.makeText(AddEditItemActivity.this,
                        getString(R.string.error_posting_item) + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSubmit.setEnabled(!loading);
        binding.buttonSubmit.setText(loading ? R.string.posting : R.string.post_item);
        binding.editItemName.setEnabled(!loading);
        binding.editLocation.setEnabled(!loading);
        binding.editDescription.setEnabled(!loading);
        binding.chipLost.setEnabled(!loading);
        binding.chipFound.setEnabled(!loading);
        binding.buttonAddPhoto.setEnabled(!loading);
    }
}