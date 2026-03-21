package com.example.eatgreen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllergiesActivity extends AppCompatActivity {

    private CheckBox cbArachide, cbGluten, cbLactose, cbFruitsMer, cbOeufs, cbSoja;
    private EditText etAutre;
    private Button btnEnregistrer;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private int userId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergies);

        cbArachide = findViewById(R.id.allergie_arachide);
        cbGluten = findViewById(R.id.allergie_gluten);
        cbLactose = findViewById(R.id.allergie_lactose);
        cbFruitsMer = findViewById(R.id.allergie_fruits_mer);
        cbOeufs = findViewById(R.id.allergie_oeufs);
        cbSoja = findViewById(R.id.allergie_soja);
        etAutre = findViewById(R.id.autre_allergie);
        btnEnregistrer = findViewById(R.id.btn_enregistrer_allergies);

        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("preferences_alimentaires", MODE_PRIVATE);

        chargerAllergies();

        btnEnregistrer.setOnClickListener(v -> enregistrerAllergies());
    }

    private void chargerAllergies() {
        cbArachide.setChecked(sharedPreferences.getBoolean("allergie_arachide", false));
        cbGluten.setChecked(sharedPreferences.getBoolean("allergie_gluten", false));
        cbLactose.setChecked(sharedPreferences.getBoolean("allergie_lactose", false));
        cbFruitsMer.setChecked(sharedPreferences.getBoolean("allergie_fruits_mer", false));
        cbOeufs.setChecked(sharedPreferences.getBoolean("allergie_oeufs", false));
        cbSoja.setChecked(sharedPreferences.getBoolean("allergie_soja", false));
        etAutre.setText(sharedPreferences.getString("allergie_autre", ""));
    }

    private void enregistrerAllergies() {
        List<String> allergies = new ArrayList<>();

        if (cbArachide.isChecked()) allergies.add("Arachide");
        if (cbGluten.isChecked()) allergies.add("Gluten");
        if (cbLactose.isChecked()) allergies.add("Lactose");
        if (cbFruitsMer.isChecked()) allergies.add("Fruits de mer");
        if (cbOeufs.isChecked()) allergies.add("Œufs");
        if (cbSoja.isChecked()) allergies.add("Soja");

        String autre = etAutre.getText().toString().trim();
        if (!autre.isEmpty()) {
            allergies.add(autre);
        }

        // Sauvegarder localement
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("allergie_arachide", cbArachide.isChecked());
        editor.putBoolean("allergie_gluten", cbGluten.isChecked());
        editor.putBoolean("allergie_lactose", cbLactose.isChecked());
        editor.putBoolean("allergie_fruits_mer", cbFruitsMer.isChecked());
        editor.putBoolean("allergie_oeufs", cbOeufs.isChecked());
        editor.putBoolean("allergie_soja", cbSoja.isChecked());
        editor.putString("allergie_autre", autre);
        editor.apply();

        // Envoyer au serveur
        String url = "http://192.168.1.40/eatgreen_api/enregistrer_allergies.php";

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("allergies", allergies);
        params.put("allergies_json", new JSONObject(params).toString());

        JSONObject jsonRequest = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Allergies enregistrées", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }
}