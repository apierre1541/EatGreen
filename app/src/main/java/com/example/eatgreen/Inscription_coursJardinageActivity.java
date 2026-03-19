package com.example.eatgreen;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Inscription_coursJardinageActivity extends AppCompatActivity {

    private LinearLayout containerCours;
    private TextView tvDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription_cours_jardinage);

        containerCours = findViewById(R.id.container_cours);
        tvDate = findViewById(R.id.tv_date);

        int jour = getIntent().getIntExtra("jour", 0);
        int mois = getIntent().getIntExtra("mois", 0);
        int annee = getIntent().getIntExtra("annee", 0);

        tvDate.setText("Cours du " + jour + "/" + mois + "/" + annee);

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

                    tvHeure.setText(heureFormatee);
                    tvTitre.setText(titre);

                    final int evenementId = id;
                    final String heureFinale = heureFormatee;
                    final String titreFinal = titre;

                    btnInscrire.setOnClickListener(v -> {
                        Toast.makeText(Inscription_coursJardinageActivity.this,
                                "Inscription à " + heureFinale + " (" + titreFinal + ")",
                                Toast.LENGTH_SHORT).show();
                        // TODO: Ajouter la logique d'inscription
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
}