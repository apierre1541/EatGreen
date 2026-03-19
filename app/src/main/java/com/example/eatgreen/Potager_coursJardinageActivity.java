package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Potager_coursJardinageActivity extends AppCompatActivity {

    private LinearLayout containerHoraires;
    private ScrollView scrollViewHoraires;
    private CalendrierFragment calendrierFragment;
    private List<Evenement> evenementsDuJour = new ArrayList<>();
    private int jourActuel, moisActuel, anneeActuel;

    // Classe pour les événements
    class Evenement {
        int id;
        String titre;
        String horaire;

        Evenement(int id, String titre, String horaire) {
            this.id = id;
            this.titre = titre;
            this.horaire = horaire;
        }
    }

    @SuppressLint({"MissingInfliedId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potager_cours_jardinage);

        containerHoraires = findViewById(R.id.container_horaires);
        scrollViewHoraires = findViewById(R.id.scrollView_horaires);

        // Cacher les horaires au début
        scrollViewHoraires.setVisibility(View.GONE);

        // ✅ 1. D'ABORD initialiser le fragment
        calendrierFragment = CalendrierFragment.newInstance("etudiant");

        // ✅ 2. ENSUITE définir le listener
        calendrierFragment.setOnDateClickListener((jour, mois, annee) -> {
            // Ouvrir une autre activité avec les détails
            Intent intent = new Intent(Potager_coursJardinageActivity.this, Inscription_coursJardinageActivity.class);
            intent.putExtra("jour", jour);
            intent.putExtra("mois", mois);
            intent.putExtra("annee", annee);

            // Optionnel : passer la liste des événements
            List<CalendrierFragment.Evenement> evenements =
                    calendrierFragment.getEvenementsPourDate(jour, mois, annee);

            if (!evenements.isEmpty()) {
                try {
                    JSONArray jsonArray = new JSONArray();
                    for (CalendrierFragment.Evenement evt : evenements) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", evt.id);
                        obj.put("titre", evt.titre);
                        obj.put("horaire", evt.horaire);
                        jsonArray.put(obj);
                    }
                    intent.putExtra("evenements", jsonArray.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            startActivity(intent);
        });

        // ✅ 3. ENFIN ajouter le fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, calendrierFragment)
                .commit();
    }

    /**
     * Affiche tous les horaires de jardinage pour une date donnée
     */
    private void afficherHorairesPourDate(int jour, int mois, int annee) {
        // Vider le conteneur
        containerHoraires.removeAllViews();
        chargerEvenementsDepuisBDD(jour, mois, annee);
    }

    /**
     * Charge les événements depuis la BDD pour une date spécifique
     */
    private void chargerEvenementsDepuisBDD(int jour, int mois, int annee) {
        String url = "http://192.168.1.40/eatgreen_api/get_evenement_par_date.php?jour=" + jour +
                "&mois=" + mois + "&annee=" + annee;

        com.android.volley.RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(this);

        com.android.volley.toolbox.JsonArrayRequest request = new com.android.volley.toolbox.JsonArrayRequest(
                com.android.volley.Request.Method.GET, url, null,
                response -> {
                    try {
                        evenementsDuJour.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Evenement evt = new Evenement(
                                    obj.getInt("id"),
                                    obj.getString("titre"),
                                    obj.getString("horaire")
                            );
                            evenementsDuJour.add(evt);
                        }

                        if (evenementsDuJour.isEmpty()) {
                            scrollViewHoraires.setVisibility(View.GONE);
                            Toast.makeText(this, "Aucun cours disponible pour cette date", Toast.LENGTH_SHORT).show();
                        } else {
                            scrollViewHoraires.setVisibility(View.VISIBLE);
                            afficherListeHoraires();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    /**
     * Affiche la liste des horaires disponibles
     */
    private void afficherListeHoraires() {
        containerHoraires.removeAllViews();

        // Titre avec la date
        TextView tvTitreSection = new TextView(this);
        tvTitreSection.setText("Cours du " + jourActuel + "/" + moisActuel + "/" + anneeActuel + " :");
        tvTitreSection.setTextSize(18);
        tvTitreSection.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitreSection.setTextColor(getResources().getColor(android.R.color.black));
        tvTitreSection.setPadding(0, 16, 0, 16);
        containerHoraires.addView(tvTitreSection);

        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);

        for (Evenement evt : evenementsDuJour) {
            // Créer un layout horizontal
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(16, 16, 16, 16);
            itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
            itemLayout.setBackgroundColor(getResources().getColor(android.R.color.white));

            // Marge entre les items
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemLayout.getLayoutParams();
            params.setMargins(0, 0, 0, 8);
            itemLayout.setLayoutParams(params);

            // Formatage de l'heure
            String heureFormatee = evt.horaire;
            try {
                Date date = inputFormat.parse(evt.horaire);
                if (date != null) {
                    heureFormatee = outputFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Variables finales pour le clic
            final int evenementId = evt.id;
            final String heureFinale = heureFormatee;
            final String titreFinal = evt.titre;

            // TextView heure
            TextView tvHeure = new TextView(this);
            tvHeure.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            tvHeure.setText(heureFinale);
            tvHeure.setTextSize(18);
            tvHeure.setTextColor(getResources().getColor(android.R.color.black));
            tvHeure.setTypeface(null, android.graphics.Typeface.BOLD);

            // TextView titre
            TextView tvTitre = new TextView(this);
            tvTitre.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    2
            ));
            tvTitre.setText(titreFinal);
            tvTitre.setTextSize(16);
            tvTitre.setTextColor(getResources().getColor(android.R.color.darker_gray));

            // Bouton inscription
            Button btnInscrire = new Button(this);
            btnInscrire.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            btnInscrire.setText("S'inscrire");
            btnInscrire.setTextSize(12);
            btnInscrire.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            btnInscrire.setTextColor(getResources().getColor(android.R.color.white));

            // Clic sur le bouton d'inscription
            btnInscrire.setOnClickListener(v -> {
                Toast.makeText(Potager_coursJardinageActivity.this,
                        "Inscription à " + heureFinale + " (" + titreFinal + ")",
                        Toast.LENGTH_SHORT).show();

                // TODO: Ajouter la logique d'inscription en BDD
                // inscrireUtilisateur(evenementId);
            });

            itemLayout.addView(tvHeure);
            itemLayout.addView(tvTitre);
            itemLayout.addView(btnInscrire);

            containerHoraires.addView(itemLayout);
        }
    }
}