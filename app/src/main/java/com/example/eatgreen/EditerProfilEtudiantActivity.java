package com.example.eatgreen;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editer_profil_etudiant);

        initViews();
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
            // On lance d'abord la sauvegarde du profil
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
        final String nom = etNom.getText().toString().trim();
        final String prenom = etPrenom.getText().toString().trim();
        final String telephone = etTelephone.getText().toString().trim();
        final String email = etMail.getText().toString().trim();

        String mode = "";
        if (cbCB.isChecked()) mode = "CB";
        else if (cbPaypal.isChecked()) mode = "Paypal";
        final String finalMode = mode;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                response -> {
                    Log.d(TAG, "Réponse Profil : " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {

                            // C'EST ICI QUE TOUT SE JOUE :
                            // Une fois le profil sauvé, on décide où aller
                            if (cbCB.isChecked()) {
                                Intent intent = new Intent(this, CbActivity.class);
                                intent.putExtra("email", email); // On passe l'email !
                                startActivity(intent);
                                finish();
                            } else if (cbPaypal.isChecked()) {
                                Intent intent = new Intent(this, PaypalActivity.class);
                                intent.putExtra("email", email); // On passe l'email !
                                startActivity(intent);
                                finish();
                            } else {
                                // Si rien n'est coché, retour à l'accueil
                                Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, EtudiantActivity.class));
                                finish();
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur JSON: " + e.getMessage());
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()) {
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
        final String email = etMail.getText().toString().trim();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_REQ_URL,
                response -> {
                    Toast.makeText(this, "Demande envoyée", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()) {
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