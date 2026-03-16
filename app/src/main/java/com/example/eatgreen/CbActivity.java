package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

    private static final String SAVE_CB_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/save_cb.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cb);

        // 1. Initialisation des vues
        etNumCarte = findViewById(R.id.etNumCarte);
        etDateExp = findViewById(R.id.etDateExp);
        etCvv = findViewById(R.id.etCvv);
        btnEnregistrer = findViewById(R.id.btnEnregistrerCB);

        // 2. Log de vérification au lancement de l'écran
        Log.e(TAG, "L'écran CbActivity est bien lancé !");

        btnEnregistrer.setOnClickListener(v -> {
            Log.e(TAG, "Le bouton Enregistrer a été cliqué !");

            String numero = etNumCarte.getText().toString().trim();

            if (numero.length() < 16) {
                etNumCarte.setError("Numéro de carte invalide (16 chiffres)");
            } else {
                saveCbToDatabase();
            }
        });
    }

    private void saveCbToDatabase() {
        // Récupérer l'email passé par l'intent
        final String email = getIntent().getStringExtra("email");

        // TRÈS IMPORTANT : Si l'email est null, on ne peut pas mettre à jour
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "ERREUR : L'email reçu de l'Intent est NULL ou VIDE !");
            Toast.makeText(this, "Erreur d'identification utilisateur", Toast.LENGTH_LONG).show();
            return;
        }

        Log.e(TAG, "Tentative d'envoi Volley pour l'email : " + email);

        final String numero = etNumCarte.getText().toString().trim();
        final String dateExp = etDateExp.getText().toString().trim();
        final String cvv = etCvv.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SAVE_CB_URL,
                response -> {
                    Log.e(TAG, "RÉPONSE DU SERVEUR : " + response);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "Carte enregistrée avec succès !", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, EtudiantActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "ECHEC PHP : " + jsonObject.getString("message"));
                            Toast.makeText(this, "Erreur : " + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "ERREUR JSON : " + e.getMessage() + " | Réponse : " + response);
                    }
                },
                error -> {
                    Log.e(TAG, "ERREUR VOLLEY : " + error.toString());
                    Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
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
}