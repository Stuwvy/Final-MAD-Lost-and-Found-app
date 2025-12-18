package com.example.back2me;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

public class CloudinaryHelper {

    private static final String TAG = "CloudinaryHelper";

    // Your Cloudinary Cloud Name
    private static final String CLOUD_NAME = "dsxialnvw";

    // Unsigned upload preset
    private static final String UPLOAD_PRESET = "back2me_unsigned";

    private static final String UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    private static final OkHttpClient client = new OkHttpClient();

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }

    /**
     * Upload bitmap (from camera) to Cloudinary
     */
    public static void uploadBitmap(Bitmap bitmap, UploadCallback callback) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();

        uploadBytes(imageBytes, callback);
    }

    /**
     * Upload URI (from gallery) to Cloudinary
     */
    public static void uploadUri(Context context, Uri uri, UploadCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                callback.onError("Cannot read image file");
                return;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            inputStream.close();

            byte[] imageBytes = baos.toByteArray();
            uploadBytes(imageBytes, callback);

        } catch (IOException e) {
            Log.e(TAG, "Error reading image", e);
            callback.onError("Error reading image: " + e.getMessage());
        }
    }

    /**
     * Upload bytes to Cloudinary
     */
    private static void uploadBytes(byte[] imageBytes, UploadCallback callback) {
        String fileName = "item_" + UUID.randomUUID().toString() + ".jpg";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("folder", "back2me_items")
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build();

        Log.d(TAG, "Uploading image to Cloudinary...");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Upload failed", e);
                callback.onError("Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        String imageUrl = json.getString("secure_url");
                        Log.d(TAG, "Upload successful: " + imageUrl);
                        callback.onSuccess(imageUrl);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Upload failed: " + response.code() + " - " + errorBody);
                    callback.onError("Upload failed: " + response.code());
                }
            }
        });
    }
}