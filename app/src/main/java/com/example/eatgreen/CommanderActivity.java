package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommanderActivity extends AppCompatActivity {

    private LinearLayout layoutHoraires;
    private RequestQueue requestQueue;
    private int restaurantId;
    private List<Horaire> listeHoraires;

    // Classe interne pour les horaires
    class Horaire {
        int id;
        String date;
        String heure;
        String nomRestaurant;
        double totalPayer;

        Horaire(int id, String date, String heure, String nomRestaurant, double totalPayer) {
            this.id = id;
            this.date = date;
            this.heure = heure;
            this.nomRestaurant = nomRestaurant;
            this.totalPayer = totalPayer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commander);

        layoutHoraires = findViewById(R.id.layout_horaires);
        requestQueue = Volley.newRequestQueue(this);
        listeHoraires = new ArrayList<>();

        // Récupérer l'ID du restaurant et les autres infos
        restaurantId = getIntent().getIntExtra("restaurant_id", 0);
        String nomRestaurant = getIntent().getStringExtra("nom_restaurant");
        double totalPayer = getIntent().getDoubleExtra("total", 0.0);

        if (restaurantId == 0) {
            Toast.makeText(this, "Erreur: ID restaurant non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chargerHoraires(nomRestaurant, totalPayer);
    }

    private void chargerHoraires(String nomRestaurant, double totalPayer) {
        String url = "http://10.138.3.92/eatgreen_api/get_date_heure.php?restaurant_id=" + restaurantId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listeHoraires.clear();
                        layoutHoraires.removeAllViews();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Horaire horaire = new Horaire(
                                    obj.getInt("id"),
                                    obj.getString("date"),
                                    obj.getString("heure"),
                                    nomRestaurant,      // ← Passer le nom
                                    totalPayer           // ← Passer le total
                            );
                            listeHoraires.add(horaire);
                        }

                        afficherHoraires();

                        if (listeHoraires.isEmpty()) {
                            TextView tvVide = new TextView(this);
                            tvVide.setText("Aucun horaire disponible");
                            tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            tvVide.setTextSize(16);
                            tvVide.setPadding(0, 50, 0, 50);
                            layoutHoraires.addView(tvVide);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Erreur: " + error.toString());
                }
        );

        requestQueue.add(request);
    }

    private void afficherHoraires() {
        layoutHoraires.removeAllViews();

        for (Horaire horaire : listeHoraires) {
            // Créer un bouton pour chaque horaire
            Button btnHoraire = new Button(this);
            btnHoraire.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            btnHoraire.setText(horaire.date + " à " + horaire.heure);
            btnHoraire.setTextSize(16);
            btnHoraire.setPadding(20, 20, 20, 20);
            btnHoraire.setBackgroundTintList(getResources().getColorStateList(R.color.black));
            btnHoraire.setTextColor(getResources().getColor(android.R.color.white));

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnHoraire.getLayoutParams();
            params.setMargins(0, 0, 0, 16);
            btnHoraire.setLayoutParams(params);

            final int horaireId = horaire.id;
            final String dateChoisie = horaire.date;
            final String heureChoisie = horaire.heure;
            final String nomResto = horaire.nomRestaurant;
            final double total = horaire.totalPayer;

            btnHoraire.setOnClickListener(v -> {
                Intent intent = new Intent(CommanderActivity.this, PaymentActivity.class);
                intent.putExtra("horaire_id", horaireId);
                intent.putExtra("date", dateChoisie);
                intent.putExtra("heure", heureChoisie);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("nom_restaurant", nomResto);
                intent.putExtra("total", total);
                startActivity(intent);
            });

            layoutHoraires.addView(btnHoraire);
        }
    }
}