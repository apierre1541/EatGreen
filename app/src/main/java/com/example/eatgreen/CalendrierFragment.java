package com.example.eatgreen;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.eatgreen.databinding.ActivityPotagerCoursJardinageBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendrierFragment extends Fragment {

    private LinearLayout container;
    private TextView t2; // Mois/Année
    private Button btnMoisPrecedent, btnMoisSuivant;

    private int anneeActuelle;
    private int moisActuel;

    // URL pour récupérer les événements depuis l'API
    private static final String GET_EVENTS_URL = "http://192.168.1.40/eatgreen_api/get_evenement.php";

    // Variables pour la date à surligner
    private int jourSurligne = -1;
    private int moisSurligne = -1;
    private int anneeSurligne = -1;

    public CalendrierFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containerBundle,
                             Bundle savedInstanceState) {

        // 1. Créer le layout principal du fragment
        LinearLayout layoutPrincipal = new LinearLayout(getContext());
        layoutPrincipal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        layoutPrincipal.setPadding(16, 16, 16, 16);

        // 2. Créer la ligne de navigation (mois précédent/suivant)
        LinearLayout ligneNavigation = new LinearLayout(getContext());
        ligneNavigation.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ligneNavigation.setOrientation(LinearLayout.HORIZONTAL);
        ligneNavigation.setGravity(Gravity.CENTER);

        btnMoisPrecedent = new Button(getContext());
        btnMoisPrecedent.setText("◀");
        btnMoisPrecedent.setTextSize(18);

        t2 = new TextView(getContext());
        t2.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        t2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        t2.setTextSize(20);
        t2.setTypeface(t2.getTypeface(), Typeface.BOLD);

        btnMoisSuivant = new Button(getContext());
        btnMoisSuivant.setText("▶");
        btnMoisSuivant.setTextSize(18);

        ligneNavigation.addView(btnMoisPrecedent);
        ligneNavigation.addView(t2);
        ligneNavigation.addView(btnMoisSuivant);
        layoutPrincipal.addView(ligneNavigation);

        // 3. Créer la ligne des jours de la semaine
        LinearLayout ligneJours = new LinearLayout(getContext());
        ligneJours.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ligneJours.setOrientation(LinearLayout.HORIZONTAL);
        ligneJours.setWeightSum(7);

        String[] nomsJours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

        for (int i = 0; i < 7; i++) {
            TextView tv = new TextView(getContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1
            ));
            tv.setText(nomsJours[i]);
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(null, Typeface.BOLD);
            ligneJours.addView(tv);
        }
        layoutPrincipal.addView(ligneJours);

        // 4. Conteneur pour les boutons des jours
        container = new LinearLayout(getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        container.setOrientation(LinearLayout.VERTICAL);
        layoutPrincipal.addView(container);

        // 5. Initialiser la date
        Calendar calendar = Calendar.getInstance();
        anneeActuelle = calendar.get(Calendar.YEAR);
        moisActuel = calendar.get(Calendar.MONTH) + 1;

        // 6. Afficher le premier mois
        afficherMois(anneeActuelle, moisActuel);

        // 7. Gestionnaires de clic
        btnMoisPrecedent.setOnClickListener(v -> {
            moisActuel--;
            if (moisActuel < 1) {
                moisActuel = 12;
                anneeActuelle--;
            }
            afficherMois(anneeActuelle, moisActuel);
        });

        btnMoisSuivant.setOnClickListener(v -> {
            moisActuel++;
            if (moisActuel > 12) {
                moisActuel = 1;
                anneeActuelle++;
            }
            afficherMois(anneeActuelle, moisActuel);
        });

        // 8. Bouton pour voir les événements créés
        Button btnVoirEvenements = new Button(getContext());
        btnVoirEvenements.setText("Voir événements");
        btnVoirEvenements.setTextColor(Color.WHITE);
        btnVoirEvenements.setBackgroundColor(Color.DKGRAY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 16; // décoller un peu du calendrier
        btnVoirEvenements.setLayoutParams(params);
        layoutPrincipal.addView(btnVoirEvenements);

        // 9. Appel quand on clique pour récupérer les événements et passer à l'activité d'affichage
        btnVoirEvenements.setOnClickListener(v -> {
            new Thread(() -> {
                try {
                    URL url = new URL(GET_EVENTS_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    conn.disconnect();

                    String json = response.toString();

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Intent intent = new Intent(getActivity(), EvenementsActivity.class);
                            intent.putExtra("jsonEvenements", json);
                            startActivity(intent);
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Erreur GET", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }).start();
        });

        return layoutPrincipal;
    }

    /**
     * Affiche le mois et crée les boutons
     */
    private void afficherMois(int annee, int mois) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(annee, mois - 1, 1);

        // Formater et afficher le mois/année
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
        t2.setText(sdf.format(calendar.getTime()));

        // Créer les boutons pour ce mois
        creerBoutonsPourMois(annee, mois);
    }

    /**
     * Crée les boutons pour tous les jours du mois
     */
    private void creerBoutonsPourMois(int annee, int mois) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(annee, mois - 1, 1);
        int nombreJours = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int joursParSemaine = 7;
        container.removeAllViews();

        LinearLayout ligneActuelle = null;

        for (int i = 1; i <= nombreJours; i++) {
            if ((i - 1) % joursParSemaine == 0) {
                ligneActuelle = new LinearLayout(getContext());
                ligneActuelle.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                ligneActuelle.setOrientation(LinearLayout.HORIZONTAL);
                ligneActuelle.setWeightSum(joursParSemaine);
                container.addView(ligneActuelle);
            }

            Button btn = new Button(getContext());
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    100,
                    1
            ));
            btn.setText(String.valueOf(i));
            btn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Couleur par défaut (bleu)
            btn.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));
            btn.setTextColor(Color.WHITE);

            final int jour = i;
            final int moisFinal = mois;
            final int anneeFinal = annee;

            btn.setOnClickListener(v -> {
                reinitialiserCouleurs(); // Réinitialiser tous les boutons en bleu

                v.setBackgroundColor(Color.GREEN); // Mettre le bouton cliqué en vert
                ((Button) v).setTextColor(Color.BLACK);

                jourSurligne = jour;
                moisSurligne = moisFinal;
                anneeSurligne = anneeFinal;

                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), FormCalendrierActivity.class);
                    intent.putExtra("jour", jour);
                    intent.putExtra("mois", moisFinal);
                    intent.putExtra("annee", anneeFinal);
                    intent.putExtra("date", jour + "/" + moisFinal + "/" + anneeFinal);
                    startActivity(intent);
                }
            });

            ligneActuelle.addView(btn);
        }
    }

    /**
     * Réinitialise tous les boutons en bleu
     */
    private void reinitialiserCouleurs() {
        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout ligne = (LinearLayout) container.getChildAt(i);
            for (int j = 0; j < ligne.getChildCount(); j++) {
                View vue = ligne.getChildAt(j);
                if (vue instanceof Button) {
                    vue.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));
                    ((Button) vue).setTextColor(Color.WHITE);
                }
            }
        }
    }

    /**
     * Récupère les événements depuis l'API et affiche le JSON dans un toast (pour test)
     */
    private void recupererEvenements() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_EVENTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                conn.disconnect();

                String json = response.toString();

                // Afficher le JSON reçu dans un toast pour test
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "JSON reçu : " + json, Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Erreur GET", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }
}