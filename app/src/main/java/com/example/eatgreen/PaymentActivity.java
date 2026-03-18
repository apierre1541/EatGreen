package com.example.eatgreen;

import android.content.SharedPreferences;
import android.os.Bundle;
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

    private TextView tvDate, tvHeure, tvNomRestaurant, tvTotalCommande;
    private RadioGroup radioGroupPaiement;
    private Button btnConfirmer, btnAnnuler;

    private int horaireId;
    private String date, heure;
    private int restaurantId;
    private double totalPayer;
    private int utilisateurId;
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
        tvNomRestaurant.setText(getIntent().getStringExtra("nom_restaurant"));
        totalPayer = getIntent().getDoubleExtra("total", 0.0);

        // Afficher le total
        tvTotalCommande.setText(String.format("%.2f €", totalPayer));
        tvDate.setText(date);
        tvHeure.setText(heure);

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        utilisateurId = prefs.getInt("user_id", 0);

        requestQueue = Volley.newRequestQueue(this);

        // Bouton annuler
        btnAnnuler.setOnClickListener(v -> finish());

        // Bouton confirmer
        btnConfirmer.setOnClickListener(v -> {
            int selectedId = radioGroupPaiement.getCheckedRadioButtonId();

            if (selectedId == R.id.radioCB) {
                Toast.makeText(this,
                        "Merci pour votre commande ! Vous paierez directement sur place.",
                        Toast.LENGTH_LONG).show();
                enregistrerCommande("CB");
            } else if (selectedId == R.id.radioPaypal) {
                Toast.makeText(this,
                        "Merci pour votre commande ! Vous allez être rediriger vers votre compte paypal.",
                        Toast.LENGTH_LONG).show();
                enregistrerCommande("PayPal");
            } else if (selectedId == R.id.radioEspeces) {
                Toast.makeText(this,
                        "Merci pour votre commande ! Vous paierez en espèces sur place.",
                        Toast.LENGTH_LONG).show();
                enregistrerCommande("Espèces");
            } else {
                Toast.makeText(this, "Veuillez choisir un mode de paiement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enregistrerCommande(String modePaiement) {
        String url = "http://10.138.3.92/eatgreen_api/enregistrer_commande.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            // Message déjà affiché avant
                            finish();
                        } else {
                            Toast.makeText(this, "Erreur: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("utilisateur_id", String.valueOf(utilisateurId));
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