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

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.example.eatgreen.databinding.ActivityPotagerCoursJardinageBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendrierFragment extends Fragment {

    private LinearLayout container;
    private TextView t2; // Mois/Année
    private Button btnMoisPrecedent, btnMoisSuivant;

    private int anneeActuelle;
    private int moisActuel;

    // Variables pour la date à surligner
    private int jourSurligne = -1;
    private int moisSurligne = -1;
    private int anneeSurligne = -1;

    public CalendrierFragment() {
        // Required empty public constructor
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
        btnMoisPrecedent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moisActuel--;
                if (moisActuel < 1) {
                    moisActuel = 12;
                    anneeActuelle--;
                }
                afficherMois(anneeActuelle, moisActuel);
            }
        });

        btnMoisSuivant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moisActuel++;
                if (moisActuel > 12) {
                    moisActuel = 1;
                    anneeActuelle++;
                }
                afficherMois(anneeActuelle, moisActuel);
            }
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
        // Déterminer le nombre de jours
        Calendar calendar = Calendar.getInstance();
        calendar.set(annee, mois - 1, 1);
        int nombreJours = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int joursParSemaine = 7;

        // Vider le conteneur
        container.removeAllViews();

        LinearLayout ligneActuelle = null;

        for (int i = 1; i <= nombreJours; i++) {
            // Créer nouvelle ligne au début et tous les 7 jours
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

            // Créer le bouton
            Button btn = new Button(getContext());
            btn.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    100,
                    1
            ));
            btn.setText(String.valueOf(i));
            btn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // ✅ Couleur par défaut (bleu)
            btn.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));
            btn.setTextColor(Color.WHITE);

            final int jour = i;
            final int moisFinal = mois;
            final int anneeFinal = annee;

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ✅ 1. Réinitialiser TOUS les boutons en bleu
                    reinitialiserCouleurs();

                    // ✅ 2. Mettre le bouton cliqué en VERT
                    v.setBackgroundColor(Color.GREEN);
                    ((Button) v).setTextColor(Color.BLACK);

                    // ✅ 3. Sauvegarder la date sélectionnée
                    jourSurligne = jour;
                    moisSurligne = moisFinal;
                    anneeSurligne = anneeFinal;

                    // ✅ 4. OUVRIR UNE NOUVELLE ACTIVITÉ
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), ActivityPotagerCoursJardinageBinding.class);

                        // Optionnel : passer la date à l'activité suivante
                        intent.putExtra("jour", jour);
                        intent.putExtra("mois", moisFinal);
                        intent.putExtra("annee", anneeFinal);
                        intent.putExtra("date", jour + "/" + moisFinal + "/" + anneeFinal);

                        startActivity(intent);
                    }
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
}