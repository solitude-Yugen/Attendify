package com.example.attendify;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class ImageProcessor {
    private static final String TAG = "ImageProcessor";
    // Update the URL to use port 5000
    private static final String SERVER_URL = "http://192.168.64.245:5000/process_image";
    private final OkHttpClient client;

    public ImageProcessor() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // Increased timeout
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public interface ImageProcessCallback {
        void onSuccess(List<String> recognizedStudents);
        void onError(String error);
    }

    public void processImage(Bitmap image, ImageProcessCallback callback) {
        try {
            // Convert bitmap to base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Create JSON request
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("image", base64Image);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    jsonBody.toString()
            );

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build();

            Log.d(TAG, "Sending request to: " + SERVER_URL);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network request failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Server response: " + responseBody);
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (jsonResponse.getBoolean("success")) {
                                JSONArray namesArray = jsonResponse.getJSONArray("recognized_students");
                                List<String> recognizedStudents = new ArrayList<>();

                                for (int i = 0; i < namesArray.length(); i++) {
                                    recognizedStudents.add(namesArray.getString(i));
                                }

                                callback.onSuccess(recognizedStudents);
                            } else {
                                callback.onError(jsonResponse.getString("error"));
                            }
                        } else {
                            callback.onError("Server error: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing response", e);
                        callback.onError("Error processing response: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparing request", e);
            callback.onError("Error preparing request: " + e.getMessage());
        }
    }
}