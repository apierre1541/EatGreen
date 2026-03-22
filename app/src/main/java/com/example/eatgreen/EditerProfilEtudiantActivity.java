package com.example.eatgreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditerProfilEtudiantActivity extends AppCompatActivity {

    private static final String TAG = "EditerProfilEtudiant";
    private static final String BASE_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/";
    private static final String UPDATE_URL = BASE_URL + "update_profile.php";
    private static final String DELETE_REQ_URL = BASE_URL + "request_delete.php";

    private EditText etNom, etPrenom, etTelephone, etMail;
    private CheckBox cbCB, cbPaypal;
    private Button btnEnregistrer, btnSupprimer;

    // Variables pour stocker les valeurs récupérées de l'Intent
    private String userNom, userPrenom, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editer_profil_etudiant);

        initViews();

        // Récupérer les données de l'Intent et les stocker
        Intent intent = getIntent();
        userNom = intent.getStringExtra("user_nom");
        userPrenom = intent.getStringExtra("user_prenom");
        userEmail = intent.getStringExtra("user_email");
        String userTelephone = intent.getStringExtra("user_telephone");

        // Afficher les données
        etNom.setText(userNom);
        etPrenom.setText(userPrenom);
        etTelephone.setText(userTelephone);
        etMail.setText(userEmail);

        // Désactiver les champs non modifiables
        etMail.setEnabled(false);
        etMail.setFocusable(false);
        etNom.setEnabled(false);
        etNom.setFocusable(false);
        etPrenom.setEnabled(false);
        etPrenom.setFocusable(false);

        etMail.setTextColor(Color.GRAY);
        etNom.setTextColor(Color.GRAY);
        etPrenom.setTextColor(Color.GRAY);

        // Le téléphone reste modifiable
        etTelephone.setEnabled(true);
        etTelephone.setFocusable(true);

        setupListeners();
    }

    private void initViews() {
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etTelephone = findViewById(R.id.etTelephone);
        etMail = findViewById(R.id.etMail);

        cbCB = findViewById(R.id.cbCarteBancaire);
        cbPaypal = findViewById(R.id.cbPaypal);

        btnEnregistrer = findViewById(R.id.btnEnregistrer);
        btnSupprimer = findViewById(R.id.btnSupprimerCompte);
    }

    private void setupListeners() {
        btnEnregistrer.setOnClickListener(v -> {
            // Vérifier qu'un mode de paiement est sélectionné
            if (!cbCB.isChecked() && !cbPaypal.isChecked()) {
                Toast.makeText(this, "Veuillez choisir un mode de paiement (CB ou PayPal)", Toast.LENGTH_SHORT).show();
                return;
            }
            saveProfileToDatabase();
        });

        btnSupprimer.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Supprimer le compte")
                    .setMessage("Voulez-vous envoyer une demande de suppression ?")
                    .setPositiveButton("Oui", (dialog, which) -> sendDeleteRequest())
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    private void saveProfileToDatabase() {
        // Utiliser les variables stockées pour le nom, prénom et email
        // car les champs sont désactivés
        final String nom = userNom;
        final String prenom = userPrenom;
        final String email = userEmail;
        final String telephone = etTelephone.getText().toString().trim();

        // Déterminer le mode de paiement
        String mode = "";
        if (cbCB.isChecked()) {
            mode = "CB";
        } else if (cbPaypal.isChecked()) {
            mode = "Paypal";
        }
        final String finalMode = mode;

        // Afficher un message de chargement
        Toast.makeText(this, "Enregistrement en cours...", Toast.LENGTH_SHORT).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                response -> {
                    Log.d(TAG, "Réponse Profil : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {

                            Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();

                            // Sauvegarder le mode de paiement dans SharedPreferences
                            android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = prefs.edit();

                            if (cbCB.isChecked()) {
                                editor.putString("mode_paiement", "CB");
                                editor.apply();

                                // Rediriger vers CbActivity
                                Intent intent = new Intent(this, CbActivity.class);
                                intent.putExtra("email", email);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();

                            } else if (cbPaypal.isChecked()) {
                                editor.putString("mode_paiement", "Paypal");
                                editor.apply();

                                // Rediriger vers PaypalActivity
                                Intent intent = new Intent(this, PaypalActivity.class);
                                intent.putExtra("email", email);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Toast.makeText(this, "Erreur: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur JSON: " + e.getMessage());
                        Toast.makeText(this, "Erreur de réponse serveur", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Erreur réseau: " + error.getMessage());
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nom", nom);
                params.put("prenom", prenom);
                params.put("telephone", telephone);
                params.put("email", email);
                params.put("mode_paiement", finalMode);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void sendDeleteRequest() {
        final String email = userEmail; // Utiliser l'email stocké

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_REQ_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "Demande de suppression envoyée avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erreur: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur JSON: " + e.getMessage());
                        Toast.makeText(this, "Erreur de réponse", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }
}