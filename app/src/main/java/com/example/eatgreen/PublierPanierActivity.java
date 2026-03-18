package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublierPanierActivity extends AppCompatActivity {

    EditText titre, condition, quantite, prix, adresse_postal, code_postal, commune;
    Button photo, b7;
    private String imagePath = "";
    private int idResto;
    private static final int REQUEST_IMAGE_PICK = 2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publier_panier);

        titre = findViewById(R.id.titre);
        b7 = findViewById(R.id.b7);
        photo = findViewById(R.id.photo);
        condition = findViewById(R.id.condition);
        quantite = findViewById(R.id.quantite);
        prix = findViewById(R.id.prix);
        adresse_postal = findViewById(R.id.adresse_postal);
        code_postal = findViewById(R.id.code_postal);
        commune = findViewById(R.id.commune);

        idResto = getIntent().getIntExtra("restaurant_id", 0);

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                envoyerDonnees();
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouvrirGalerie();
            }
        });
    }

    private void ouvrirGalerie() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            imagePath = data.getData().toString();
            photo.setText("✓ Image sélectionnée");
            Toast.makeText(this, "Image sélectionnée", Toast.LENGTH_SHORT).show();
        }
    }

    private void envoyerDonnees() {
        if (titre.getText().toString().isEmpty() ||
                condition.getText().toString().isEmpty() ||
                quantite.getText().toString().isEmpty() ||
                prix.getText().toString().isEmpty() ||
                adresse_postal.getText().toString().isEmpty() ||
                code_postal.getText().toString().isEmpty() ||
                commune.getText().toString().isEmpty()) {

            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        b7.setEnabled(false);
        b7.setText("Envoi en cours...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    MultipartBody.Builder builder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("nom_plat", titre.getText().toString())
                            .addFormDataPart("condition_repas", condition.getText().toString())
                            .addFormDataPart("quantite", quantite.getText().toString())
                            .addFormDataPart("prix", prix.getText().toString())
                            .addFormDataPart("adresse_postal", adresse_postal.getText().toString())
                            .addFormDataPart("code_postal", code_postal.getText().toString())
                            .addFormDataPart("commune", commune.getText().toString());

                    if (!imagePath.isEmpty()) {
                        Uri uri = Uri.parse(imagePath);
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        byte[] imageBytes = getBytes(inputStream);

                        builder.addFormDataPart("photo", "image.jpg",
                                RequestBody.create(MediaType.parse("image/jpeg"), imageBytes));
                    }

                    RequestBody requestBody = builder.build();

                    Request request = new Request.Builder()
                            .url("http://10.138.3.92/eatgreen_api/panier_repas.php")
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();
                    String responseBody = response.body().string();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            b7.setEnabled(true);
                            b7.setText("Publier un repas");

                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                if (jsonResponse.getBoolean("success")) {
                                    Toast.makeText(PublierPanierActivity.this,
                                            jsonResponse.getString("message"),
                                            Toast.LENGTH_LONG).show();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(PublierPanierActivity.this, DateHeureActivity.class);
                                            intent.putExtra("restaurant_id", idResto);
                                            startActivity(intent);
                                        }
                                    }, 1500);

                                } else {
                                    Toast.makeText(PublierPanierActivity.this,jsonResponse.getString("message"),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(PublierPanierActivity.this,
                                        "Erreur de parsing", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            b7.setEnabled(true);
                            b7.setText("Publier un repas");
                            Toast.makeText(PublierPanierActivity.this,
                                    "Erreur: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}