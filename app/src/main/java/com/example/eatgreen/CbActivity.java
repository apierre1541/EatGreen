package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CbActivity extends AppCompatActivity {

    private static final String TAG = "DEBUG_PHP";
    private EditText etNumCarte, etDateExp, etCvv;
    private Button btnEnregistrer;

    private String email;

    private static final String SAVE_CB_URL = "http://192.168.1.40/eatgreen_api/save_cb.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cb);

        // Initialisation des vues
        etNumCarte = findViewById(R.id.etNumCarte);
        etDateExp = findViewById(R.id.etDateExp);
        etCvv = findViewById(R.id.etCvv);
        btnEnregistrer = findViewById(R.id.btnEnregistrerCB);

        // Récupérer l'email depuis l'Intent ou SharedPreferences
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        if (email == null || email.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            email = prefs.getString("user_email", "");
        }

        // Charger les infos de carte existantes
        chargerInfosCarteExistantes();

        btnEnregistrer.setOnClickListener(v -> {
            String numero = etNumCarte.getText().toString().trim();
            String dateExp = etDateExp.getText().toString().trim();
            String cvv = etCvv.getText().toString().trim();

            // Validation des champs
            if (numero.isEmpty() || numero.length() < 16) {
                etNumCarte.setError("Numéro de carte invalide (16 chiffres)");
                return;
            }

            if (dateExp.isEmpty()) {
                etDateExp.setError("Date d'expiration requise (MM/AA)");
                return;
            }

            if (cvv.isEmpty() || cvv.length() < 3) {
                etCvv.setError("CVV invalide (3 chiffres)");
                return;
            }

            // Sauvegarder les infos de carte
            saveCbToDatabase(numero, dateExp, cvv);
        });
    }

    private void chargerInfosCarteExistantes() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String numeroCarte = prefs.getString("cb_numero", "");
        String dateExp = prefs.getString("cb_date", "");
        String cvv = prefs.getString("cb_cvv", "");

        if (!numeroCarte.isEmpty()) {
            etNumCarte.setText(numeroCarte);
        }
        if (!dateExp.isEmpty()) {
            etDateExp.setText(dateExp);
        }
        if (!cvv.isEmpty()) {
            etCvv.setText(cvv);
        }
    }

    private void saveCbToDatabase(final String numero, final String dateExp, final String cvv) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Erreur d'identification utilisateur", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SAVE_CB_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            sauvegarderInfosCarteLocalement(numero, dateExp, cvv);
                            Toast.makeText(this, "Carte enregistrée avec succès !", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, EtudiantActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur : " + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de réponse serveur", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("cb_numero", numero);
                params.put("cb_date", dateExp);
                params.put("cb_cvv", cvv);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void sauvegarderInfosCarteLocalement(String numero, String dateExp, String cvv) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cb_numero", numero);
        editor.putString("cb_date", dateExp);
        editor.putString("cb_cvv", cvv);
        editor.putString("mode_paiement", "CB");
        editor.apply();
    }
}