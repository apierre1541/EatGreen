package com.example.eatgreen;

import android.os.Bundle;
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ListeInscritCoursJardinageActivity extends AppCompatActivity {

    private LinearLayout containerInscrits;
    private TextView tvTitre;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_inscrits);

        containerInscrits = findViewById(R.id.container_inscrits);
        tvTitre = findViewById(R.id.tv_titre);
        requestQueue = Volley.newRequestQueue(this);

        tvTitre.setText("Tous les cours et inscriptions");

        // Charger TOUS les cours sans filtre de date
        chargerTousLesCours();
    }

    /**
     * Charge TOUS les cours avec leurs inscrits
     */
    private void chargerTousLesCours() {
        String url = "http://192.168.1.40/eatgreen_api/get_inscrits_par_cours.php"; // Sans paramètres

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        containerInscrits.removeAllViews();

                        if (response.length() == 0) {
                            TextView tvVide = new TextView(ListeInscritCoursJardinageActivity.this);
                            tvVide.setText("Aucun cours disponible");
                            tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            tvVide.setTextSize(16);
                            tvVide.setPadding(0, 50, 0, 50);
                            containerInscrits.addView(tvVide);
                            return;
                        }

                        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject cours = response.getJSONObject(i);

                            int coursId = cours.getInt("id");
                            String titre = cours.getString("titre");
                            String horaire = cours.getString("horaire");
                            JSONArray inscrits = cours.getJSONArray("inscrits");

                            // Récupérer la date si disponible
                            String dateInfo = "";
                            if (cours.has("jour") && cours.has("mois") && cours.has("annee")) {
                                int jour = cours.getInt("jour");
                                int mois = cours.getInt("mois");
                                int annee = cours.getInt("annee");
                                dateInfo = jour + "/" + mois + "/" + annee + " - ";
                            }

                            // Formatage de l'heure
                            String heureFormatee = horaire;
                            try {
                                Date date = inputFormat.parse(horaire);
                                if (date != null) {
                                    heureFormatee = outputFormat.format(date);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            // Titre du cours avec date
                            TextView tvCoursTitre = new TextView(this);
                            tvCoursTitre.setText(dateInfo + heureFormatee + " - " + titre);
                            tvCoursTitre.setTextSize(18);
                            tvCoursTitre.setTypeface(null, android.graphics.Typeface.BOLD);
                            tvCoursTitre.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            tvCoursTitre.setPadding(0, 16, 0, 8);
                            containerInscrits.addView(tvCoursTitre);

                            if (inscrits.length() == 0) {
                                // Aucun inscrit pour ce cours
                                TextView tvAucun = new TextView(this);
                                tvAucun.setText("   Aucun inscrit");
                                tvAucun.setTextSize(14);
                                tvAucun.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                tvAucun.setPadding(16, 4, 0, 8);
                                containerInscrits.addView(tvAucun);
                            } else {
                                // Afficher le nombre d'inscrits
                                TextView tvNbInscrits = new TextView(this);
                                tvNbInscrits.setText(inscrits.length() + " inscrits");
                                tvNbInscrits.setTextSize(14);
                                tvNbInscrits.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                                tvNbInscrits.setPadding(16, 4, 0, 8);
                                containerInscrits.addView(tvNbInscrits);

                                // Afficher tous les inscrits pour ce cours
                                for (int j = 0; j < inscrits.length(); j++) {
                                    JSONObject inscrit = inscrits.getJSONObject(j);
                                    String nom = inscrit.getString("nom");
                                    String prenom = inscrit.getString("prenom");
                                    String email = inscrit.getString("email");

                                    View itemView = getLayoutInflater().inflate(R.layout.item_inscrit, containerInscrits, false);

                                    TextView tvNom = itemView.findViewById(R.id.tv_nom);
                                    TextView tvEmail = itemView.findViewById(R.id.tv_email);

                                    tvNom.setText("   • " + prenom + " " + nom);
                                    tvEmail.setText("     " + email);

                                    containerInscrits.addView(itemView);
                                }
                            }

                            // Ligne de séparation
                            View separator = new View(this);
                            separator.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    1
                            ));
                            separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                            separator.setPadding(0, 8, 0, 8);
                            containerInscrits.addView(separator);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ListeInscritCoursJardinageActivity.this, "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(ListeInscritCoursJardinageActivity.this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(request);
    }
}