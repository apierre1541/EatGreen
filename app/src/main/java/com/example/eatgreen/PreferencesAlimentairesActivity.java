package com.example.eatgreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PreferencesAlimentairesActivity extends AppCompatActivity {

    private CheckBox cbVegetarien, cbVegan, cbAllergie, cbIntolerance;
    private Button btnEnregistrer;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private int userId = 1; // À remplacer par l'ID de l'utilisateur connecté

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_alimentaires);

        // Initialisation des vues
        cbVegetarien = findViewById(R.id.vegetarien);
        cbVegan = findViewById(R.id.vegan);
        cbAllergie = findViewById(R.id.allergie);
        cbIntolerance = findViewById(R.id.intolerance);
        btnEnregistrer = findViewById(R.id.btnPreferencesAlimentaires);

        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("preferences_alimentaires", MODE_PRIVATE);

        // Charger les préférences existantes
        chargerPreferences();

        // Gestion du clic sur Allergies
        cbAllergie.setOnClickListener(v -> {
            if (cbAllergie.isChecked()) {
                Intent intent = new Intent(PreferencesAlimentairesActivity.this, AllergiesActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        // Gestion du clic sur Intolérances
        cbIntolerance.setOnClickListener(v -> {
            if (cbIntolerance.isChecked()) {
                Intent intent = new Intent(PreferencesAlimentairesActivity.this, IntolerancesActivity.class);
                startActivityForResult(intent, 2);
            }
        });

        // Enregistrement des préférences
        btnEnregistrer.setOnClickListener(v -> enregistrerPreferences());
    }

    private void chargerPreferences() {
        cbVegetarien.setChecked(sharedPreferences.getBoolean("vegetarien", false));
        cbVegan.setChecked(sharedPreferences.getBoolean("vegan", false));

        boolean aAllergies = sharedPreferences.getBoolean("a_allergies", false);
        boolean aIntolerances = sharedPreferences.getBoolean("a_intolerances", false);

        cbAllergie.setChecked(aAllergies);
        cbIntolerance.setChecked(aIntolerances);
    }

    private void enregistrerPreferences() {
        boolean vegetarien = cbVegetarien.isChecked();
        boolean vegan = cbVegan.isChecked();
        boolean aAllergies = cbAllergie.isChecked();
        boolean aIntolerances = cbIntolerance.isChecked();

        // Sauvegarder localement
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("vegetarien", vegetarien);
        editor.putBoolean("vegan", vegan);
        editor.putBoolean("a_allergies", aAllergies);
        editor.putBoolean("a_intolerances", aIntolerances);
        editor.apply();

        // Envoyer au serveur
        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/enregistrer_preferences.php";

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("vegetarien", vegetarien);
        params.put("vegan", vegan);
        params.put("a_allergies", aAllergies);
        params.put("a_intolerances", aIntolerances);

        JSONObject jsonRequest = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Préférences enregistrées avec succès", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur: " + response.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_LONG).show());

        requestQueue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // Retour des allergies
                cbAllergie.setChecked(true);
            } else if (requestCode == 2) {
                // Retour des intolérances
                cbIntolerance.setChecked(true);
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == 1) {
                cbAllergie.setChecked(false);
            } else if (requestCode == 2) {
                cbIntolerance.setChecked(false);
            }
        }
    }
}