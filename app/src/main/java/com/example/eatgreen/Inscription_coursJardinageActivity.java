package com.example.eatgreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Inscription_coursJardinageActivity extends AppCompatActivity {

    private LinearLayout containerCours;
    private TextView tvDate;
    private RequestQueue requestQueue;
    private int jour, mois, annee;
    private int utilisateurId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription_cours_jardinage);

        containerCours = findViewById(R.id.container_cours);
        tvDate = findViewById(R.id.tv_date);

        // ✅ SUPPRIMEZ ces lignes si le bouton n'existe pas
        // Button btnVoirInscrits = findViewById(R.id.btn_voir_inscrits);

        requestQueue = Volley.newRequestQueue(this);

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        utilisateurId = prefs.getInt("user_id", 0);

        jour = getIntent().getIntExtra("jour", 0);
        mois = getIntent().getIntExtra("mois", 0);
        annee = getIntent().getIntExtra("annee", 0);

        tvDate.setText("Cours du " + jour + "/" + mois + "/" + annee);

        // ✅ SUPPRIMEZ ce bloc si le bouton n'existe pas
        /*
        btnVoirInscrits.setOnClickListener(v -> {
            Intent intent = new Intent(Inscription_coursJardinageActivity.this, ListeInscritCoursJardinageActivity.class);
            intent.putExtra("jour", jour);
            intent.putExtra("mois", mois);
            intent.putExtra("annee", annee);
            startActivity(intent);
        });
        */

        // Récupérer la liste des événements
        String evenementsJson = getIntent().getStringExtra("evenements");

        if (evenementsJson != null && !evenementsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(evenementsJson);

                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int id = obj.getInt("id");
                    String titre = obj.getString("titre");
                    String horaire = obj.getString("horaire");

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

                    // Créer un item pour chaque cours
                    View coursView = getLayoutInflater().inflate(R.layout.item_cours, containerCours, false);

                    TextView tvHeure = coursView.findViewById(R.id.tv_heure);
                    TextView tvTitre = coursView.findViewById(R.id.tv_titre);
                    Button btnInscrire = coursView.findViewById(R.id.btn_inscrire);
                    TextView tvStatut = coursView.findViewById(R.id.tv_statut);

                    tvHeure.setText(heureFormatee);
                    tvTitre.setText(titre);

                    final int evenementId = id;
                    final String heureFinale = heureFormatee;
                    final String titreFinal = titre;

                    // Vérifier si l'utilisateur est déjà inscrit
                    verifierInscription(evenementId, tvStatut, btnInscrire);

                    btnInscrire.setOnClickListener(v -> {
                        inscrireUtilisateur(evenementId, heureFinale, titreFinal, tvStatut, btnInscrire);
                    });

                    containerCours.addView(coursView);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Aucun cours
            TextView tvVide = new TextView(this);
            tvVide.setText("Aucun cours pour cette date");
            tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvVide.setTextSize(16);
            tvVide.setPadding(0, 50, 0, 50);
            containerCours.addView(tvVide);
        }
    }

    private void verifierInscription(int evenementId, TextView tvStatut, Button btnInscrire) {
        String url = "http://192.168.1.40/eatgreen_api/verifier_inscription.php?evenement_id=" + evenementId + "&user_id=" + utilisateurId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        // Déjà inscrit
                        tvStatut.setVisibility(View.VISIBLE);
                        tvStatut.setText("✅ Inscrit");
                        tvStatut.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        btnInscrire.setVisibility(View.GONE);
                    } else {
                        // Non inscrit
                        tvStatut.setVisibility(View.GONE);
                        btnInscrire.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    error.printStackTrace();
                }
        );

        requestQueue.add(request);
    }

    private void inscrireUtilisateur(int evenementId, String heure, String titre, TextView tvStatut, Button btnInscrire) {
        String url = "http://192.168.1.40/eatgreen_api/inscrire_cours.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(Inscription_coursJardinageActivity.this,
                                    "Inscription réussie à " + heure + " (" + titre + ")",
                                    Toast.LENGTH_SHORT).show();

                            // Mettre à jour l'affichage
                            tvStatut.setVisibility(View.VISIBLE);
                            tvStatut.setText("✅ Inscrit");
                            tvStatut.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            btnInscrire.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(Inscription_coursJardinageActivity.this,
                                    "Erreur: " + json.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(Inscription_coursJardinageActivity.this,
                            "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("evenement_id", String.valueOf(evenementId));
                params.put("user_id", String.valueOf(utilisateurId));
                params.put("jour", String.valueOf(jour));
                params.put("mois", String.valueOf(mois));
                params.put("annee", String.valueOf(annee));
                return params;
            }
        };

        requestQueue.add(request);
    }
}