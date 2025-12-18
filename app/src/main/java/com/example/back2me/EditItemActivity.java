package com.example.back2me;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.back2me.databinding.ActivityEditItemBinding;
import com.google.firebase.auth.FirebaseAuth;

public class EditItemActivity extends AppCompatActivity {

    private ActivityEditItemBinding binding;
    private Handler mainHandler;

    private String itemId;
    private Item currentItem;
    private String selectedStatus = "lost";
    private Uri selectedImageUri = null;
    private Bitmap selectedBitmap = null;
    private String currentImageUrl = "";
    private boolean imageChanged = false;
    private boolean isUploading = false;

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    selectedBitmap = bitmap;
                    selectedImageUri = null;
                    imageChanged = true;
                    displaySelectedImage(bitmap);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    selectedBitmap = null;
                    imageChanged = true;
                    displaySelectedImage(uri);
                }
            });

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
        binding = ActivityEditItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainHandler = new Handler(Looper.getMainLooper());

        itemId = getIntent().getStringExtra("ITEM_ID");
        if (itemId == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
        loadItem();
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

    private void loadItem() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ItemRepository.getItemById(itemId, new ItemRepository.SingleItemCallback() {
            @Override
            public void onSuccess(Item item) {
                binding.progressBar.setVisibility(View.GONE);
                currentItem = item;
                populateFields(item);
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditItemActivity.this, "Error loading item", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(Item item) {
        binding.editItemName.setText(item.getName());
        binding.editLocation.setText(item.getLocation());
        binding.editDescription.setText(item.getDescription());

        selectedStatus = item.getStatus();
        if ("found".equalsIgnoreCase(selectedStatus)) {
            binding.chipFound.setChecked(true);
            binding.chipLost.setChecked(false);
        } else {
            binding.chipLost.setChecked(true);
            binding.chipFound.setChecked(false);
        }

        currentImageUrl = item.getImageUrl();
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            binding.cardImagePreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(currentImageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.imagePreview);
            binding.buttonAddPhoto.setText(R.string.change_photo);
        } else {
            binding.cardImagePreview.setVisibility(View.GONE);
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Change Photo")
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
        Glide.with(this).load(uri).centerCrop().into(binding.imagePreview);
        binding.buttonAddPhoto.setText(R.string.change_photo);
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        selectedBitmap = null;
        currentImageUrl = "";
        imageChanged = true;
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
            return;
        }
        if (location.isEmpty()) {
            binding.editLocation.setError(getString(R.string.error_location_required));
            return;
        }
        if (isUploading) {
            Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (imageChanged) {
            if (selectedBitmap != null) {
                uploadBitmapAndUpdate(name, location, description);
            } else if (selectedImageUri != null) {
                uploadUriAndUpdate(name, location, description);
            } else {
                updateItem(name, location, description, "");
            }
        } else {
            updateItem(name, location, description, currentImageUrl);
        }
    }

    private void uploadBitmapAndUpdate(String name, String location, String description) {
        isUploading = true;
        CloudinaryHelper.uploadBitmap(selectedBitmap, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                mainHandler.post(() -> {
                    isUploading = false;
                    updateItem(name, location, description, imageUrl);
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    isUploading = false;
                    setLoading(false);
                    Toast.makeText(EditItemActivity.this, "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void uploadUriAndUpdate(String name, String location, String description) {
        isUploading = true;
        CloudinaryHelper.uploadUri(this, selectedImageUri, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                mainHandler.post(() -> {
                    isUploading = false;
                    updateItem(name, location, description, imageUrl);
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    isUploading = false;
                    setLoading(false);
                    Toast.makeText(EditItemActivity.this, "Upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateItem(String name, String location, String description, String imageUrl) {
        currentItem.setName(name);
        currentItem.setLocation(location);
        currentItem.setDescription(description);
        currentItem.setStatus(selectedStatus);
        currentItem.setImageUrl(imageUrl);

        ItemRepository.updateItem(currentItem, new ItemRepository.UpdateCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(EditItemActivity.this, R.string.item_updated_successfully, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(EditItemActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonSubmit.setEnabled(!loading);
        binding.buttonSubmit.setText(loading ? R.string.updating : R.string.update_item);
    }
}