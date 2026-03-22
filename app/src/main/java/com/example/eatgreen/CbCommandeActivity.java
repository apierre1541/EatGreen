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

public class CbCommandeActivity extends AppCompatActivity {

    private static final String TAG = "CB_COMMANDE";  // ← Ce tag existe maintenant
    private EditText etNumCarte, etDateExp, etCvv;
    private Button btnEnregistrer;

    private TextView tvNomRestaurant, tvDate, tvHeure;

    private int utilisateurId;
    private int horaireId;
    private int restaurantId;
    private String dateCommande;
    private String heureCommande;
    private double totalPayer;
    private String email;
    private String nomRestaurant;

    private static final String SAVE_CB_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/save_cb.php";
    private static final String CREER_COMMANDE_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/creer_commande.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cb_commande);

        // Initialisation des vues
        etNumCarte = findViewById(R.id.etNumCarte);
        etDateExp = findViewById(R.id.etDateExp);
        etCvv = findViewById(R.id.etCvv);
        btnEnregistrer = findViewById(R.id.btnEnregistrerCB);

        tvNomRestaurant = findViewById(R.id.tv_nom_restaurant);
        tvDate = findViewById(R.id.tv_date);
        tvHeure = findViewById(R.id.tv_heure);

        // Récupérer les données
        recupererDonnees();

        // Afficher les informations
        afficherInfosCommande();

        // Charger les infos de carte existantes
        chargerInfosCarteExistantes();

        btnEnregistrer.setOnClickListener(v -> {
            String numero = etNumCarte.getText().toString().trim();
            String dateExp = etDateExp.getText().toString().trim();
            String cvv = etCvv.getText().toString().trim();

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

            saveCbToDatabase(numero, dateExp, cvv);
        });
    }

    private void recupererDonnees() {
        Intent intent = getIntent();

        utilisateurId = intent.getIntExtra("utilisateur_id", 0);
        horaireId = intent.getIntExtra("horaire_id", 0);
        restaurantId = intent.getIntExtra("restaurant_id", 0);
        dateCommande = intent.getStringExtra("date");
        heureCommande = intent.getStringExtra("heure");
        totalPayer = intent.getDoubleExtra("total", 0);
        email = intent.getStringExtra("email");
        nomRestaurant = intent.getStringExtra("nom_restaurant");

        // AFFICHER LES LOGS ICI
        Log.d(TAG, "=== RÉCUPÉRATION DES DONNÉES ===");
        Log.d(TAG, "utilisateurId: " + utilisateurId);
        Log.d(TAG, "email: " + email);
        Log.d(TAG, "restaurantId: " + restaurantId);
        Log.d(TAG, "horaireId: " + horaireId);
        Log.d(TAG, "dateCommande: " + dateCommande);
        Log.d(TAG, "heureCommande: " + heureCommande);
        Log.d(TAG, "totalPayer: " + totalPayer);
        Log.d(TAG, "nomRestaurant: " + nomRestaurant);
        Log.d(TAG, "==============================");

        // Si l'email est vide, récupérer depuis SharedPreferences
        if (email == null || email.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            email = prefs.getString("user_email", "");
            Log.d(TAG, "Email récupéré depuis SharedPreferences: " + email);
        }

        // Si l'ID utilisateur est vide, récupérer depuis SharedPreferences
        if (utilisateurId == 0) {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            utilisateurId = prefs.getInt("user_id", 0);
            Log.d(TAG, "ID récupéré depuis SharedPreferences: " + utilisateurId);
        }
    }

    private void afficherInfosCommande() {
        if (tvNomRestaurant != null && nomRestaurant != null) {
            tvNomRestaurant.setText(nomRestaurant);
        }
        if (tvDate != null && dateCommande != null) {
            tvDate.setText(dateCommande);
        }
        if (tvHeure != null && heureCommande != null) {
            tvHeure.setText(heureCommande);
        }
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
                            finaliserCommande();
                        } else {
                            Toast.makeText(this, "Erreur : " + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
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

    private void finaliserCommande() {
        // AFFICHER LES LOGS ICI
        Log.d(TAG, "=== FINALISATION COMMANDE ===");
        Log.d(TAG, "utilisateurId = " + utilisateurId);
        Log.d(TAG, "email = " + email);
        Log.d(TAG, "restaurantId = " + restaurantId);
        Log.d(TAG, "horaireId = " + horaireId);
        Log.d(TAG, "date = " + dateCommande);
        Log.d(TAG, "heure = " + heureCommande);
        Log.d(TAG, "total = " + totalPayer);
        Log.d(TAG, "============================");

        if (utilisateurId == 0 || horaireId == 0 || restaurantId == 0) {
            Toast.makeText(this, "Erreur: Données de commande manquantes", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, CREER_COMMANDE_URL,
                response -> {
                    Log.d(TAG, "Réponse du serveur: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "Commande finalisée avec succès !", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(this, EtudiantActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur: " + json.optString("message", "Erreur inconnue"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Erreur de parsing", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("utilisateur_id", String.valueOf(utilisateurId));
                params.put("email", email);
                params.put("restaurant_id", String.valueOf(restaurantId));
                params.put("horaire_id", String.valueOf(horaireId));
                params.put("date", dateCommande != null ? dateCommande : "");
                params.put("heure", heureCommande != null ? heureCommande : "");
                params.put("mode_paiement", "Carte Bancaire");
                params.put("total", String.valueOf(totalPayer));
                params.put("statut", "finalise");
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}