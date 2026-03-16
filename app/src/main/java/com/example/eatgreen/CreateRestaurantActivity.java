package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateRestaurantActivity extends AppCompatActivity {

    private EditText etNomRestaurant, etSiret, etAdresse, etCodePostal, etCommune;
    private Button btnCreateRestaurant;
    private ImageView btnBack;

    private int userId;
    private String userEmail, userNom, userPrenom;

    private static final String BASE_URL = "http://10.138.3.92/eatgreen_api/";
    private static final String CREATE_RESTAURANT_URL = BASE_URL + "create_restaurant.php";
    private static final String TAG = "CreateRestaurantActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_restaurant);

        // Récupérer les données de l'intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("user_id", 0);
        userEmail = intent.getStringExtra("user_email");
        userNom = intent.getStringExtra("user_nom");
        userPrenom = intent.getStringExtra("user_prenom");

        if (userId == 0) {
            Toast.makeText(this, "Erreur: utilisateur non identifié", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etNomRestaurant = findViewById(R.id.etNomRestaurant);
        etSiret = findViewById(R.id.etSiret);
        etAdresse = findViewById(R.id.etAdresse);
        etCodePostal = findViewById(R.id.etCodePostal);
        etCommune = findViewById(R.id.etCommune);
        btnCreateRestaurant = findViewById(R.id.btnCreateRestaurant);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnCreateRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRestaurant();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createRestaurant() {
        final String nomRestaurant = etNomRestaurant.getText().toString().trim();
        final String siret = etSiret.getText().toString().trim();
        final String adresse = etAdresse.getText().toString().trim();
        final String codePostal = etCodePostal.getText().toString().trim();
        final String commune = etCommune.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(nomRestaurant)) {
            etNomRestaurant.setError("Nom du restaurant requis");
            return;
        }

        if (TextUtils.isEmpty(siret)) {
            etSiret.setError("N° SIRET requis");
            return;
        }

        if (siret.length() != 14) {
            etSiret.setError("Le SIRET doit contenir 14 chiffres");
            return;
        }

        if (TextUtils.isEmpty(adresse)) {
            etAdresse.setError("Adresse requise");
            return;
        }

        if (TextUtils.isEmpty(codePostal)) {
            etCodePostal.setError("Code postal requis");
            return;
        }

        if (codePostal.length() != 5) {
            etCodePostal.setError("Code postal invalide (5 chiffres)");
            return;
        }

        if (TextUtils.isEmpty(commune)) {
            etCommune.setError("Commune requise");
            return;
        }

        // Désactiver le bouton
        btnCreateRestaurant.setEnabled(false);
        btnCreateRestaurant.setText("Création...");

        // Paramètres de la requête
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        params.put("nom_restaurant", nomRestaurant);
        params.put("siret", siret);
        params.put("adresse", adresse);
        params.put("code_postal", codePostal);
        params.put("commune", commune);

        Log.d(TAG, "Paramètres: " + params.toString());

        // Requête Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CREATE_RESTAURANT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        btnCreateRestaurant.setEnabled(true);
                        btnCreateRestaurant.setText("CRÉER MON RESTAURANT");

                        Log.d(TAG, "Réponse: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                Toast.makeText(CreateRestaurantActivity.this,
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();

                                // Rediriger vers la connexion
                                Intent intent = new Intent(CreateRestaurantActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(CreateRestaurantActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CreateRestaurantActivity.this,
                                    "Erreur de réponse", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btnCreateRestaurant.setEnabled(true);
                        btnCreateRestaurant.setText("CRÉER MON RESTAURANT");

                        String message = "Erreur réseau";
                        if (error.networkResponse != null) {
                            message += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(CreateRestaurantActivity.this, message, Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}