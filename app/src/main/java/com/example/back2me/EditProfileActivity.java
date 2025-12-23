package com.example.back2me;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.back2me.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ActivityEditProfileBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Bitmap selectedBitmap;
    private String currentPhotoUrl;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            selectedBitmap = BitmapFactory.decodeStream(inputStream);
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            binding.imageProfile.setImageBitmap(selectedBitmap);
                            Log.d(TAG, "Image selected from gallery");
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading image", e);
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        selectedBitmap = (Bitmap) extras.get("data");
                        binding.imageProfile.setImageBitmap(selectedBitmap);
                        Log.d(TAG, "Image captured from camera");
                    }
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserData();
        setupClickListeners();
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        // Load email (non-editable)
        binding.textEmail.setText(user.getEmail());

        // Load display name
        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            binding.editDisplayName.setText(displayName);
        }

        // Load profile photo
        Uri photoUrl = user.getPhotoUrl();
        if (photoUrl != null) {
            currentPhotoUrl = photoUrl.toString();
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_profile_circle)
                    .error(R.drawable.ic_profile_circle)
                    .circleCrop()
                    .into(binding.imageProfile);
        }

        // Load phone from Firestore (optional)
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String phone = document.getString("phone");
                        if (phone != null) {
                            binding.editPhone.setText(phone);
                        }
                    }
                });
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.buttonChangePhoto.setOnClickListener(v -> showImagePicker());

        // Also allow clicking on the image itself
        binding.imageProfile.setOnClickListener(v -> showImagePicker());

        binding.buttonSave.setOnClickListener(v -> saveProfile());
    }

    private void showImagePicker() {
        String[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery)};

        new AlertDialog.Builder(this)
                .setTitle(R.string.change_photo)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermission();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(intent);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void saveProfile() {
        String displayName = binding.editDisplayName.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();

        if (displayName.isEmpty()) {
            binding.editDisplayName.setError(getString(R.string.display_name_required));
            binding.editDisplayName.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.buttonSave.setEnabled(false);

        // Check if we need to upload an image
        if (selectedBitmap != null) {
            uploadImageAndSave(displayName, phone);
        } else {
            updateProfile(displayName, phone, currentPhotoUrl);
        }
    }

    private void uploadImageAndSave(String displayName, String phone) {
        Log.d(TAG, "Uploading image to Cloudinary...");

        CloudinaryHelper.uploadBitmap(selectedBitmap, new CloudinaryHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Image uploaded successfully: " + imageUrl);
                runOnUiThread(() -> updateProfile(displayName, phone, imageUrl));
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Image upload failed: " + error);
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSave.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this,
                            "Failed to upload image: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void updateProfile(String displayName, String phone, String photoUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            binding.progressBar.setVisibility(View.GONE);
            binding.buttonSave.setEnabled(true);
            return;
        }

        Log.d(TAG, "Updating Firebase profile...");

        // Build profile update request
        UserProfileChangeRequest.Builder profileBuilder = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName);

        if (photoUrl != null && !photoUrl.isEmpty()) {
            profileBuilder.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(profileBuilder.build())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firebase Auth profile updated");
                    // Also save to Firestore
                    saveToFirestore(user.getUid(), displayName, phone, photoUrl);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase Auth update failed", e);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveToFirestore(String userId, String displayName, String phone, String photoUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", displayName);
        userData.put("phone", phone);
        userData.put("email", auth.getCurrentUser().getEmail());
        if (photoUrl != null) {
            userData.put("photoUrl", photoUrl);
        }

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore profile updated");
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSave.setEnabled(true);
                    Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();

                    // Set result to notify previous activity
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore update failed", e);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.buttonSave.setEnabled(true);
                    // Profile updated in Auth even if Firestore fails
                    Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}