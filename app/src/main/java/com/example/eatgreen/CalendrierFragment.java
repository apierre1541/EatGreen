package com.example.eatgreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendrierFragment extends Fragment {

    private LinearLayout container;
    private TextView t2; // Mois/Année
    private Button btnMoisPrecedent, btnMoisSuivant;

    private int anneeActuelle;
    private int moisActuel;

    // URLs API
    private static final String GET_EVENTS_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/get_evenement.php";
    private static final String GET_DATES_VERTES_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/get_dates_vertes.php";
    private static final String AJOUTER_DATE_VERTE_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/ajouter_date_verte.php";

    // Variables
    private Set<String> datesEnVert = new HashSet<>();
    private int utilisateurId;
    private String role;
    private RequestQueue requestQueue;

    // Stockage des événements
    private Map<String, List<Evenement>> evenementsParDate = new HashMap<>();

    // Interface pour communiquer avec l'activité
    public interface OnDateClickListener {
        void onDateClick(int jour, int mois, int annee);
    }

    private OnDateClickListener dateClickListener;

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.dateClickListener = listener;
    }

    // Classe interne pour les événements
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

    // Factory method pour créer le fragment avec le rôle
    public static CalendrierFragment newInstance(String role) {
        CalendrierFragment fragment = new CalendrierFragment();
        Bundle args = new Bundle();
        args.putString("role", role);
        fragment.setArguments(args);
        return fragment;
    }

    public CalendrierFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup containerBundle,
                             Bundle savedInstanceState) {

        // Récupérer le rôle
        if (getArguments() != null) {
            role = getArguments().getString("role", "etudiant");
        }

        // Récupérer l'ID de l'utilisateur
        SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", getContext().MODE_PRIVATE);
        utilisateurId = prefs.getInt("user_id", 0);
        requestQueue = Volley.newRequestQueue(getContext());

        // Charger les données
        chargerDatesVertes();
        chargerTousEvenements();

        // Créer le layout principal
        LinearLayout layoutPrincipal = new LinearLayout(getContext());
        layoutPrincipal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        layoutPrincipal.setPadding(16, 16, 16, 16);

        // Ligne de navigation
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

        // Jours de la semaine
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

        // Conteneur pour les jours
        container = new LinearLayout(getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        container.setOrientation(LinearLayout.VERTICAL);
        layoutPrincipal.addView(container);

        // Initialiser la date
        Calendar calendar = Calendar.getInstance();
        anneeActuelle = calendar.get(Calendar.YEAR);
        moisActuel = calendar.get(Calendar.MONTH) + 1;

        // Afficher le premier mois
        afficherMois(anneeActuelle, moisActuel);

        // Navigation mois
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

        return layoutPrincipal;
    }

    /**
     * Charge tous les événements
     */
    private void chargerTousEvenements() {
        String url = GET_EVENTS_URL;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        evenementsParDate.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String titre = obj.getString("titre");
                            String horaire = obj.getString("horaire");
                            int jour = obj.getInt("jour");
                            int mois = obj.getInt("mois");
                            int annee = obj.getInt("annee");

                            String dateKey = jour + "/" + mois + "/" + annee;

                            if (!evenementsParDate.containsKey(dateKey)) {
                                evenementsParDate.put(dateKey, new ArrayList<>());
                            }
                            evenementsParDate.get(dateKey).add(new Evenement(id, titre, horaire));
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> afficherMois(anneeActuelle, moisActuel));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("CALENDRIER", "Erreur chargement événements", error)
        );

        requestQueue.add(request);
    }

    /**
     * Charge les dates vertes
     */
    private void chargerDatesVertes() {
        String url = GET_DATES_VERTES_URL + "?user_id=" + utilisateurId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray dates = response.getJSONArray("dates");
                            datesEnVert.clear();
                            for (int i = 0; i < dates.length(); i++) {
                                JSONObject obj = dates.getJSONObject(i);
                                String dateKey = obj.getInt("jour") + "/" +
                                        obj.getInt("mois") + "/" +
                                        obj.getInt("annee");
                                datesEnVert.add(dateKey);
                            }
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> afficherMois(anneeActuelle, moisActuel));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("CALENDRIER", "Erreur chargement dates vertes", error)
        );

        requestQueue.add(request);
    }

    /**
     * Affiche le mois
     */
    private void afficherMois(int annee, int mois) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(annee, mois - 1, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.FRENCH);
        t2.setText(sdf.format(calendar.getTime()));
        creerBoutonsPourMois(annee, mois);
    }

    /**
     * Crée les boutons des jours (SANS points rouges)
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
            btn.setLayoutParams(new LinearLayout.LayoutParams(0, 100, 1));
            btn.setText(String.valueOf(i));
            btn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            final int jour = i;
            final int moisFinal = mois;
            final int anneeFinal = annee;
            String dateKey = jour + "/" + moisFinal + "/" + anneeFinal;

            // ✅ LOGIQUE CORRECTE POUR LES COULEURS
            if ("admin".equals(role)) {
                // Pour l'admin : les dates qu'il a marquées en vert
                if (datesEnVert.contains(dateKey)) {
                    btn.setBackgroundColor(Color.GREEN);
                    btn.setTextColor(Color.BLACK);
                } else {
                    btn.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));
                    btn.setTextColor(Color.WHITE);
                }
            } else {
                // Pour l'étudiant : les dates qui ont des événements (créés par admin)
                if (evenementsParDate.containsKey(dateKey)) {
                    btn.setBackgroundColor(Color.GREEN);  // ← VERT pour les cours disponibles
                    btn.setTextColor(Color.BLACK);
                } else {
                    btn.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));
                    btn.setTextColor(Color.WHITE);
                }
            }

            btn.setOnClickListener(v -> {
                // ACTION SELON LE RÔLE
                if (getActivity() != null) {
                    if ("admin".equals(role)) {
                        // Admin : marque en vert et ouvre formulaire
                        if (!datesEnVert.contains(dateKey)) {
                            marquerDateVerte(jour, moisFinal, anneeFinal, btn);
                        }
                        Intent intent = new Intent(getActivity(), FormCalendrierActivity.class);
                        intent.putExtra("jour", jour);
                        intent.putExtra("mois", moisFinal);
                        intent.putExtra("annee", anneeFinal);
                        intent.putExtra("date", dateKey);
                        intent.putExtra("role", role);
                        startActivity(intent);
                    } else {
                        // ÉTUDIANT : vérifie s'il y a des événements
                        if (evenementsParDate.containsKey(dateKey)) {
                            // Utiliser le listener
                            if (dateClickListener != null) {
                                dateClickListener.onDateClick(jour, moisFinal, anneeFinal);
                            }
                        } else {
                            Toast.makeText(getContext(), "Aucun cours pour cette date", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            ligneActuelle.addView(btn);
        }
    }

    /**
     * Marque une date en vert
     */
    private void marquerDateVerte(int jour, int mois, int annee, Button btn) {
        String url = AJOUTER_DATE_VERTE_URL;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            String dateKey = jour + "/" + mois + "/" + annee;
                            datesEnVert.add(dateKey);
                            btn.setBackgroundColor(Color.GREEN);
                            btn.setTextColor(Color.BLACK);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(utilisateurId));
                params.put("jour", String.valueOf(jour));
                params.put("mois", String.valueOf(mois));
                params.put("annee", String.valueOf(annee));
                return params;
            }
        };

        requestQueue.add(request);
    }

    // Méthode pour obtenir les événements d'une date (utile pour l'activité)
    public List<Evenement> getEvenementsPourDate(int jour, int mois, int annee) {
        String dateKey = jour + "/" + mois + "/" + annee;
        return evenementsParDate.getOrDefault(dateKey, new ArrayList<>());
    }
}