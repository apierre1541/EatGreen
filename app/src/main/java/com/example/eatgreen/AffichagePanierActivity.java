package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AffichagePanierActivity extends AppCompatActivity {

    private LinearLayout layout;
    private List<Plat> listePlats;
    private ImageButton btnPanier;
    private RequestQueue requestQueue;

    private int utilisateurId;

    // Classe interne Plat
    // Classe interne Plat
    class Plat {
        String nom, photo, condition, adresse, codePostal, commune, nomRestaurant;  // ← AJOUTER
        int prix, id, quantiteDisponible, restaurantId;

        Plat(String nom, String photo, String condition, int prix,
             String adresse, String codePostal, String commune,
             int id, int quantiteDisponible, int restaurantId, String nomRestaurant) {  // ← NOUVEAU PARAMÈTRE
            this.nom = nom;
            this.photo = photo;
            this.condition = condition;
            this.prix = prix;
            this.adresse = adresse;
            this.codePostal = codePostal;
            this.commune = commune;
            this.id = id;
            this.quantiteDisponible = quantiteDisponible;
            this.restaurantId = restaurantId;
            this.nomRestaurant = nomRestaurant;
        }
    }

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affichage_panier);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        utilisateurId = prefs.getInt("user_id", -1);

        if (utilisateurId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        layout = findViewById(R.id.layout);
        btnPanier = findViewById(R.id.pannier);  // ← C'est un ImageButton
        listePlats = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        // Ouvrir le panier au clic sur l'ImageButton
        if (btnPanier != null) {
            btnPanier.setOnClickListener(v -> ouvrirPanier());
        }

        chargerPlats();
    }

    private void chargerPlats() {
        String url = "http://192.168.1.40/eatgreen_api/get_panier_repas.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listePlats.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            // Récupérer le nom du restaurant (avec valeur par défaut)
                            String nomRestaurant = obj.optString("nom_restaurant", "Restaurant inconnu");

                            listePlats.add(new Plat(
                                    obj.getString("nom_plat"),
                                    obj.getString("photo"),
                                    obj.getString("condition_repas"),
                                    obj.getInt("prix"),
                                    obj.getString("adresse_postal"),
                                    obj.getString("code_postal"),
                                    obj.getString("commune"),
                                    obj.getInt("id"),
                                    obj.getInt("quantite"),
                                    obj.getInt("restaurant_id"),
                                    nomRestaurant  // ← AJOUTER
                            ));
                        }
                        getPanierDepuisServeur();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()
        );
        requestQueue.add(request);
    }
    private void getPanierDepuisServeur() {
        String url = "http://192.168.1.40/eatgreen_api/get_panier.php?users_id=" + utilisateurId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        HashMap<Integer, Integer> panierQuantites = new HashMap<>();
                        HashMap<Integer, Integer> quantitesRestantes = new HashMap<>(); // ← NOUVEAU

                        if (response.getBoolean("success") && response.has("articles")) {
                            JSONArray articles = response.getJSONArray("articles");
                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject article = articles.getJSONObject(i);
                                int platId = article.getInt("plat_id");
                                int quantite = article.getInt("quantite");
                                int quantiteRestante = article.getInt("quantite_restante"); // ← AJOUTER

                                panierQuantites.put(platId, quantite);
                                quantitesRestantes.put(platId, quantiteRestante);
                            }
                        }
                        afficherPlats(panierQuantites, quantitesRestantes);

                        majBadgePanier(response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        afficherPlats(new HashMap<>(), new HashMap<>());
                    }
                },
                error -> {
                    Log.e("ERROR", "Erreur get panier: " + error.toString());
                    afficherPlats(new HashMap<>(), new HashMap<>());
                }
        );

        requestQueue.add(request);
    }

    private void afficherPlats(HashMap<Integer, Integer> panierQuantites,
                               HashMap<Integer, Integer> quantitesRestantes) {
        layout.removeAllViews();

        for (Plat plat : listePlats) {
            try {
                View platView = getLayoutInflater().inflate(R.layout.item_plat, layout, false);

                TextView tvNom = platView.findViewById(R.id.nom_plats);
                ImageView tvPhoto = platView.findViewById(R.id.photo);
                TextView tvCondition = platView.findViewById(R.id.condition_plats);
                TextView tvPrix = platView.findViewById(R.id.prix);
                TextView tvAdresse = platView.findViewById(R.id.adresse_postal);
                TextView tvCodePostal = platView.findViewById(R.id.code_postal);
                TextView tvCommune = platView.findViewById(R.id.commune);
                Button btnAjouter = platView.findViewById(R.id.ajout_pannier);
                TextView tvQuantiteRestante = platView.findViewById(R.id.quantite_restante);

                // ✅ NOUVEAU TextView pour le nom du restaurant
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView tvRestaurant = platView.findViewById(R.id.tv_nom_restaurant);

                // Remplir les vues
                tvNom.setText(plat.nom);
                tvCondition.setText("Condition: " + plat.condition);
                tvPrix.setText("Prix: " + plat.prix + "€");
                tvAdresse.setText("Adresse: " + plat.adresse);
                tvCodePostal.setText("Code postal: " + plat.codePostal);
                tvCommune.setText("Commune: " + plat.commune);

                // ✅ Afficher le nom du restaurant
                if (tvRestaurant != null) {
                    tvRestaurant.setText("Restaurant: " + plat.nomRestaurant);
                }

                // Gérer la photo
                if (tvPhoto != null) {
                    if (plat.photo != null && !plat.photo.isEmpty()) {
                        String imageUrl = "http://10.138.3.92/eatgreen_api/uploads/" + plat.photo;
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_menu_camera)
                                .error(R.drawable.ic_menu_camera)
                                .into(tvPhoto);
                    } else {
                        tvPhoto.setImageResource(R.drawable.ic_menu_camera);
                    }
                }

                // GESTION DU BOUTON (le reste inchangé)
                if (btnAjouter != null) {
                    int quantiteDansPanier = panierQuantites.containsKey(plat.id) ? panierQuantites.get(plat.id) : 0;
                    int quantiteRestante = quantitesRestantes.containsKey(plat.id) ?
                            quantitesRestantes.get(plat.id) : plat.quantiteDisponible;

                    if (tvQuantiteRestante != null) {
                        tvQuantiteRestante.setText("Disponible: " + quantiteRestante + "/" + plat.quantiteDisponible);
                    }

                    if (quantiteRestante <= 0) {
                        btnAjouter.setText("Rupture de stock");
                        btnAjouter.setEnabled(false);
                        btnAjouter.setAlpha(0.5f);
                        btnAjouter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
                    } else if (quantiteDansPanier > 0) {
                        btnAjouter.setText("✓ Dans le panier (x" + quantiteDansPanier + ")");
                        btnAjouter.setEnabled(false);
                        btnAjouter.setAlpha(0.5f);
                        btnAjouter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                    } else {
                        btnAjouter.setText("Ajouter au panier");
                        btnAjouter.setEnabled(true);
                        btnAjouter.setAlpha(1.0f);
                        btnAjouter.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E91E63")));
                    }

                    final int platId = plat.id;
                    final String nomPlat = plat.nom;
                    final Button btn = btnAjouter;
                    final int quantiteRestanteFinale = quantiteRestante;

                    btnAjouter.setOnClickListener(v -> {
                        btn.setEnabled(false);
                        if (quantiteRestanteFinale > 0 && quantiteDansPanier == 0) {
                            ajouterAuPanier(platId, 1, btn, nomPlat);
                        }
                    });
                }

                layout.addView(platView);

            } catch (Exception e) {
                Log.e("ERROR", "Erreur d'affichage: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void ajouterAuPanier(int platId, int quantite, Button btn, String nomPlat) {
        String url = "http://192.168.1.40/eatgreen_api/ajouter_panier.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("AJOUT", "Réponse: " + response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            // Récupérer la quantité restante
                            int quantiteRestante = json.getInt("quantite_restante");
                            int totalDansPanier = json.getInt("total_articles");

                            // ✅ RÉCUPÉRER LES INFOS DU RESTAURANT
                            int restoId = json.getInt("restaurant_id");
                            String restoNom = json.getString("nom_restaurant");

                            Log.d("AJOUT", "Restaurant: " + restoNom + " (ID: " + restoId + ")");

                            Toast.makeText(this, nomPlat + " ajouté (" + totalDansPanier + "/" +
                                    (totalDansPanier + quantiteRestante) + ")", Toast.LENGTH_SHORT).show();

                            // Mettre à jour le bouton
                            majBoutonPanier(btn, totalDansPanier);

                            // Si plus de stock disponible, désactiver définitivement
                            if (quantiteRestante <= 0) {
                                btn.setText("Rupture de stock");
                                btn.setEnabled(false);
                                btn.setAlpha(0.5f);
                                btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
                            }

                            getPanierDepuisServeur();
                        } else {
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btn.setEnabled(true);
                    }
                },
                error -> {
                    Log.e("AJOUT", "Erreur réseau", error);
                    Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                    btn.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("users_id", String.valueOf(utilisateurId));
                params.put("plat_id", String.valueOf(platId));
                params.put("quantite", String.valueOf(quantite));
                Log.d("AJOUT", "Params: " + params);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void retirerDuPanier(int articleId, Button btn, String nomPlat) {
        String url = "http://192.168.1.40/eatgreen_api/retirer_panier.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "❌ " + nomPlat + " retiré du panier", Toast.LENGTH_SHORT).show();
                            majBoutonPanier(btn, 0);
                            getPanierDepuisServeur();
                        } else {
                            Toast.makeText(this, "Erreur: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                            btn.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btn.setEnabled(true);
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    btn.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("users_id", String.valueOf(utilisateurId));
                params.put("article_id", String.valueOf(articleId));
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void majBoutonPanier(Button btn, int quantite) {
        if (quantite > 0) {
            btn.setText("✓ Dans le panier (x" + quantite + ")");
            btn.setEnabled(false);  // Désactivé car déjà dans le panier
            btn.setAlpha(0.5f);
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        } else {
            btn.setText("Ajouter au panier");
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E91E63")));
        }
    }
    private void majBadgePanier(JSONObject panierData) {
        try {
            if (panierData.getBoolean("success")) {
                int totalArticles = 0;
                if (panierData.has("articles")) {
                    JSONArray articles = panierData.getJSONArray("articles");
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        totalArticles += article.getInt("quantite");
                    }
                }

                // Pour ImageButton, on ne peut pas setText, donc on change l'image ou on ajoute un badge
                if (totalArticles > 0) {
                    // Option 1: Changer l'icône pour une version avec badge
                    // btnPanier.setImageResource(R.drawable.cadis_avec_badge);

                    // Option 2: Ajouter un overlay (plus complexe)
                    // Pour l'instant, on garde juste l'icône
                    btnPanier.setImageResource(R.drawable.cadis);

                    // On peut aussi changer la teinte
                    btnPanier.setColorFilter(Color.parseColor("#4CAF50"));
                } else {
                    btnPanier.setImageResource(R.drawable.cadis);
                    btnPanier.setColorFilter(Color.parseColor("#E91E63"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
    }
}

    private void ouvrirPanier() {
        Intent intent = new Intent(AffichagePanierActivity.this, PanierActivity.class);
        intent.putExtra("users_id", utilisateurId);
        startActivity(intent);
    }
}