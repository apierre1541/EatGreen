package com.example.eatgreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PAYMENT_ACTIVITY";

    private TextView tvDate, tvHeure, tvNomRestaurant, tvTotalCommande;
    private RadioGroup radioGroupPaiement;
    private Button btnConfirmer, btnAnnuler;

    private int horaireId;
    private String date, heure;
    private int restaurantId;
    private double totalPayer;
    private int utilisateurId;
    private String email;
    private String nomRestaurant;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialiser les vues
        tvDate = findViewById(R.id.tv_date_commande);
        tvHeure = findViewById(R.id.tv_heure_commande);
        tvNomRestaurant = findViewById(R.id.tv_nom_restaurant);
        tvTotalCommande = findViewById(R.id.tv_total_commande);
        radioGroupPaiement = findViewById(R.id.radioGroupPaiement);
        btnConfirmer = findViewById(R.id.btn_confirmer);
        btnAnnuler = findViewById(R.id.btn_annuler);

        // Récupérer les données
        horaireId = getIntent().getIntExtra("horaire_id", 0);
        date = getIntent().getStringExtra("date");
        heure = getIntent().getStringExtra("heure");
        restaurantId = getIntent().getIntExtra("restaurant_id", 0);
        nomRestaurant = getIntent().getStringExtra("nom_restaurant");
        tvNomRestaurant.setText(nomRestaurant);
        totalPayer = getIntent().getDoubleExtra("total", 0.0);

        // Afficher le total
        tvTotalCommande.setText(String.format("%.2f €", totalPayer));
        tvDate.setText(date);
        tvHeure.setText(heure);

        // Récupérer l'ID et l'email de l'utilisateur connecté
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        utilisateurId = prefs.getInt("user_id", 0);
        email = prefs.getString("user_email", "");

        // Vérifier l'email
        if (email == null || email.isEmpty()) {
            email = getIntent().getStringExtra("email");
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "ERREUR: Email non trouvé !");
                Toast.makeText(this, "Erreur: Email utilisateur non trouvé", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        Log.d(TAG, "=== DONNÉES RÉCUPÉRÉES ===");
        Log.d(TAG, "utilisateurId: " + utilisateurId);
        Log.d(TAG, "email: " + email);
        Log.d(TAG, "horaireId: " + horaireId);
        Log.d(TAG, "restaurantId: " + restaurantId);
        Log.d(TAG, "nomRestaurant: " + nomRestaurant);
        Log.d(TAG, "date: " + date);
        Log.d(TAG, "heure: " + heure);
        Log.d(TAG, "totalPayer: " + totalPayer);
        Log.d(TAG, "=========================");

        requestQueue = Volley.newRequestQueue(this);

        // Bouton annuler
        btnAnnuler.setOnClickListener(v -> finish());

        // Bouton confirmer
        btnConfirmer.setOnClickListener(v -> {
            int selectedId = radioGroupPaiement.getCheckedRadioButtonId();

            if (selectedId == R.id.radioCB) {
                Intent intent = new Intent(this, CbCommandeActivity.class);
                intent.putExtra("utilisateur_id", utilisateurId);
                intent.putExtra("email", email);
                intent.putExtra("horaire_id", horaireId);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("nom_restaurant", nomRestaurant);
                intent.putExtra("date", date);
                intent.putExtra("heure", heure);
                intent.putExtra("total", totalPayer);
                startActivity(intent);
                finish();

            } else if (selectedId == R.id.radioPaypal) {
                Intent intent = new Intent(this, PaypalActivity.class);
                intent.putExtra("utilisateur_id", utilisateurId);
                intent.putExtra("email", email);
                intent.putExtra("horaire_id", horaireId);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("nom_restaurant", nomRestaurant);
                intent.putExtra("date", date);
                intent.putExtra("heure", heure);
                intent.putExtra("total", totalPayer);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Veuillez choisir un mode de paiement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enregistrerCommande(String modePaiement) {
        String url = "http://192.168.1.40/eatgreen_api/enregistrer_commande.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "Commande enregistrée avec succès !", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                params.put("date", date);
                params.put("heure", heure);
                params.put("mode_paiement", modePaiement);
                params.put("total", String.valueOf(totalPayer));
                return params;
            }
        };

        requestQueue.add(request);
    }
}