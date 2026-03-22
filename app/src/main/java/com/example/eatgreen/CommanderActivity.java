package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommanderActivity extends AppCompatActivity {

    private LinearLayout layoutHoraires;
    private ScrollView scrollView;
    private TextView tvTitre, tvRestaurant, tvTotal;
    private RequestQueue requestQueue;
    private int restaurantId;
    private String nomRestaurant;
    private double totalPayer;
    private List<Horaire> listeHoraires;

    // Classe interne pour les horaires
    class Horaire {
        int id;
        String date;
        String heure;
        String dateFormatee;

        Horaire(int id, String date, String heure) {
            this.id = id;
            this.date = date;
            this.heure = heure;
            this.dateFormatee = formaterDate(date);
        }

        private String formaterDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH);
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateStr;
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commander);

        // Initialisation des vues
        scrollView = findViewById(R.id.scrollView);
        layoutHoraires = findViewById(R.id.layout_horaires);
        tvTitre = findViewById(R.id.tv_titre);
        tvRestaurant = findViewById(R.id.tv_restaurant);
        tvTotal = findViewById(R.id.tv_total);

        requestQueue = Volley.newRequestQueue(this);
        listeHoraires = new ArrayList<>();

        // Récupérer les données de PanierActivity
        restaurantId = getIntent().getIntExtra("restaurant_id", 0);
        nomRestaurant = getIntent().getStringExtra("nom_restaurant");
        totalPayer = getIntent().getDoubleExtra("total", 0.0);

        // Afficher les informations (texte en noir)
        tvTitre.setText("Choisissez votre créneau horaire");
        tvTitre.setTextColor(Color.BLACK);
        tvRestaurant.setText(nomRestaurant);
        tvRestaurant.setTextColor(Color.BLACK);
        tvTotal.setText(String.format("Total: %.2f €", totalPayer));
        tvTotal.setTextColor(Color.BLACK);

        // LOG POUR VÉRIFIER
        Log.d("COMMANDER", "=== DONNÉES REÇUES ===");
        Log.d("COMMANDER", "restaurantId: " + restaurantId);
        Log.d("COMMANDER", "nomRestaurant: " + nomRestaurant);
        Log.d("COMMANDER", "totalPayer: " + totalPayer);
        Log.d("COMMANDER", "=====================");

        if (restaurantId == 0) {
            Toast.makeText(this, "Erreur: ID restaurant non trouvé", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chargerHoraires();
    }

    private void chargerHoraires() {
        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/get_date_heure.php?restaurant_id=" + restaurantId;

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
                                    obj.getString("heure")
                            );
                            listeHoraires.add(horaire);
                        }

                        afficherHoraires();

                        if (listeHoraires.isEmpty()) {
                            afficherMessageVide();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                        afficherMessageVide();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Erreur: " + error.toString());
                    afficherMessageVide();
                }
        );

        requestQueue.add(request);
    }

    private void afficherHoraires() {
        layoutHoraires.removeAllViews();

        // Ajouter un titre pour les horaires (texte noir)
        TextView tvSousTitre = new TextView(this);
        tvSousTitre.setText("Horaires disponibles :");
        tvSousTitre.setTextSize(16);
        tvSousTitre.setTextColor(Color.BLACK);
        tvSousTitre.setTypeface(null, Typeface.BOLD);
        tvSousTitre.setPadding(16, 16, 16, 8);
        layoutHoraires.addView(tvSousTitre);

        for (Horaire horaire : listeHoraires) {
            // Créer un bouton personnalisé pour chaque horaire (bordure et texte noir)
            Button btnHoraire = new Button(this);
            btnHoraire.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Formater le texte du bouton
            String texteBouton = horaire.dateFormatee + "  • " + horaire.heure.substring(0, 5);
            btnHoraire.setText(texteBouton);
            btnHoraire.setTextSize(16);
            btnHoraire.setPadding(24, 20, 24, 20);
            btnHoraire.setTextColor(Color.BLACK);
            btnHoraire.setTypeface(null, Typeface.NORMAL);
            btnHoraire.setAllCaps(false);
            btnHoraire.setBackgroundColor(Color.WHITE);

            // Ajouter une bordure
            btnHoraire.setBackgroundResource(android.R.drawable.editbox_background);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnHoraire.getLayoutParams();
            params.setMargins(24, 0, 24, 16);
            btnHoraire.setLayoutParams(params);

            final int horaireId = horaire.id;
            final String dateChoisie = horaire.date;
            final String heureChoisie = horaire.heure;

            btnHoraire.setOnClickListener(v -> {
                // Log pour vérifier les données envoyées
                Log.d("COMMANDER", "=== ENVOI VERS PaymentActivity ===");
                Log.d("COMMANDER", "horaire_id: " + horaireId);
                Log.d("COMMANDER", "date: " + dateChoisie);
                Log.d("COMMANDER", "heure: " + heureChoisie);
                Log.d("COMMANDER", "restaurant_id: " + restaurantId);
                Log.d("COMMANDER", "nom_restaurant: " + nomRestaurant);
                Log.d("COMMANDER", "total: " + totalPayer);
                Log.d("COMMANDER", "=================================");

                Intent intent = new Intent(CommanderActivity.this, PaymentActivity.class);
                intent.putExtra("horaire_id", horaireId);
                intent.putExtra("date", dateChoisie);
                intent.putExtra("heure", heureChoisie);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("nom_restaurant", nomRestaurant);
                intent.putExtra("total", totalPayer);
                startActivity(intent);
            });

            layoutHoraires.addView(btnHoraire);
        }
    }

    private void afficherMessageVide() {
        layoutHoraires.removeAllViews();

        TextView tvVide = new TextView(this);
        tvVide.setText("😔 Aucun horaire disponible\nVeuillez réessayer plus tard");
        tvVide.setGravity(Gravity.CENTER);
        tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvVide.setTextSize(16);
        tvVide.setTextColor(Color.GRAY);
        tvVide.setPadding(32, 50, 32, 50);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(24, 50, 24, 50);
        tvVide.setLayoutParams(params);

        layoutHoraires.addView(tvVide);
    }
}