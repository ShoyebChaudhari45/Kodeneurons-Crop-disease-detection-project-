package com.example.cropdiseasedetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView resultText;
    private Spinner languageSpinner;
    private Uri imageUri;
    ProgressBar progressBar;
    private static final int PICK_IMAGE = 100;
    private static final int CAPTURE_IMAGE = 101;

    private String[] languages = {
            "English (en)",
            "Hindi (hi)",
            "Marathi (mr)",
            "Tamil (ta)"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        imageView = findViewById(R.id.imageVieW);
        resultText = findViewById(R.id.resultText);
        Button selectBtn = findViewById(R.id.selectBtn);
        Button cameraBtn = findViewById(R.id.cameraBtn);
        Button uploadBtn = findViewById(R.id.uploadBtn);
        languageSpinner = findViewById(R.id.languageSpinner);
        progressBar = findViewById(R.id.progressBar);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Select from Gallery
        selectBtn.setOnClickListener(v -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, PICK_IMAGE);
        });

        // Capture from Camera
        cameraBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAPTURE_IMAGE);
            }
        });

        // Upload Image
        uploadBtn.setOnClickListener(v -> {
            if (imageUri != null) {
                String selectedItem = languageSpinner.getSelectedItem().toString();
                String selectedLang = "en"; // default
                if (selectedItem.contains("(hi)")) selectedLang = "hi";
                else if (selectedItem.contains("(mr)")) selectedLang = "mr";
                else if (selectedItem.contains("(ta)")) selectedLang = "ta";
                progressBar.setVisibility(View.VISIBLE);
                uploadImage(imageUri, selectedLang);
            } else {
                Toast.makeText(this, "Please select or capture an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                imageUri = data.getData();
                imageView.setImageURI(imageUri);
            }
            else if (requestCode == CAPTURE_IMAGE && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);

                // Save bitmap to cache and get URI
                Uri tempUri = FileUtils.getImageUri(this, photo);
                imageUri = tempUri;
            }
        }
    }

    private void uploadImage(Uri uri, String languageCode) {
        File file = new File(FileUtils.getPath(this, uri));
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        // Language support for API
        RequestBody lang = RequestBody.create(MediaType.parse("text/plain"), languageCode);

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<PredictionResponse> call = api.uploadImage(body, lang);
        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    PredictionResponse result = response.body();
                    resultText.setText("Disease: " + result.getDisease() +
                            "\nDescription: " + result.getDescription() +
                            "\nTreatment: " + result.getTreatment());

                    // Fade in animation
                    resultText.setAlpha(0f);
                    resultText.animate().alpha(1f).setDuration(500).start();
                } else {
                    Toast.makeText(UploadActivity.this, "Error in response", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                Toast.makeText(UploadActivity.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
