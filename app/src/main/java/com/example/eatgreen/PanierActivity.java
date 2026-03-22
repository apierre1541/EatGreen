package com.example.eatgreen;

import android.annotation.SuppressLint;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PanierActivity extends AppCompatActivity {

    private LinearLayout layoutPanier;
    private TextView tvTotal;
    private Button btnCommander, btnVider;
    private RequestQueue requestQueue;
    private int restaurantId = 0;
    private int utilisateurId;
    private String nomRestaurant = "";
    private double totalGeneral = 0;  // ← Ajouter cette variable pour stocker le total

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);

        layoutPanier = findViewById(R.id.layout_panier);
        tvTotal = findViewById(R.id.tv_total);
        btnCommander = findViewById(R.id.btn_commander);
        btnVider = findViewById(R.id.btn_vider);
        requestQueue = Volley.newRequestQueue(this);

        utilisateurId = getIntent().getIntExtra("users_id", 1);

        chargerPanier();

        btnVider.setOnClickListener(v -> viderPanier());

        btnCommander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (restaurantId == 0) {
                    Toast.makeText(PanierActivity.this,
                            "Aucun restaurant associé au panier", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Utiliser la variable totalGeneral au lieu d'extraire du TextView
                Log.d("PANIER", "=== ENVOI VERS CommanderActivity ===");
                Log.d("PANIER", "restaurantId: " + restaurantId);
                Log.d("PANIER", "nomRestaurant: " + nomRestaurant);
                Log.d("PANIER", "totalGeneral: " + totalGeneral);
                Log.d("PANIER", "====================================");

                Intent intent = new Intent(PanierActivity.this, CommanderActivity.class);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("nom_restaurant", nomRestaurant);
                intent.putExtra("total", totalGeneral);  // ← Utiliser totalGeneral
                startActivity(intent);
            }
        });
    }

    private void chargerPanier() {
        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/get_panier.php?users_id=" + utilisateurId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        layoutPanier.removeAllViews();

                        if (response.getBoolean("success")) {

                            // Récupérer le total directement depuis la réponse
                            totalGeneral = response.optDouble("total", 0);  // ← Stocker dans la variable

                            JSONArray articles = response.optJSONArray("articles");

                            if (articles == null || articles.length() == 0) {
                                afficherPanierVide();
                                return;
                            }

                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject article = articles.getJSONObject(i);
                                View itemView = getLayoutInflater().inflate(R.layout.item_panier, layoutPanier, false);

                                TextView tvNom = itemView.findViewById(R.id.tv_nom_plat);
                                TextView tvQuantite = itemView.findViewById(R.id.tv_quantite);
                                TextView tvPrix = itemView.findViewById(R.id.tv_prix);
                                Button btnRetirer = itemView.findViewById(R.id.btn_retirer);

                                String nom = article.optString("nom_plat", "Inconnu");
                                int quantite = article.optInt("quantite", 0);
                                double totalLigne = article.optDouble("total_ligne", 0);
                                int articleId = article.optInt("article_id", 0);

                                // Récupération des infos restaurant
                                if (i == 0) {
                                    restaurantId = response.optInt("restaurant_id", 0);
                                    nomRestaurant = response.optString("nom_restaurant", "Restaurant inconnu");
                                    Log.d("PANIER", "Restaurant: " + nomRestaurant + " (ID: " + restaurantId + ")");
                                }

                                tvNom.setText(nom);
                                tvQuantite.setText("x" + quantite);
                                tvPrix.setText(String.format("%.2f€", totalLigne));

                                final int finalArticleId = articleId;
                                btnRetirer.setOnClickListener(v -> retirerArticle(finalArticleId));

                                layoutPanier.addView(itemView);
                            }

                            // Afficher le total
                            tvTotal.setText(String.format("Total: %.2f€", totalGeneral));
                            Log.d("PANIER", "Total récupéré: " + totalGeneral);

                        } else {
                            afficherPanierVide();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        afficherPanierVide();
                    }
                },
                error -> {
                    afficherPanierVide();
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void afficherPanierVide() {
        layoutPanier.removeAllViews();
        totalGeneral = 0;  // ← Réinitialiser le total
        TextView tvVide = new TextView(this);
        tvVide.setText("Votre panier est vide");
        tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvVide.setTextSize(18);
        tvVide.setPadding(0, 50, 0, 50);
        layoutPanier.addView(tvVide);
        tvTotal.setText("Total: 0.00€");
    }

    private void retirerArticle(int articleId) {
        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/retirer_panier.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "Article retiré du panier", Toast.LENGTH_SHORT).show();
                            chargerPanier(); // Recharger l'affichage
                        } else {
                            Toast.makeText(this, "Erreur: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                    Log.e("RETIRER", "Erreur: " + error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("users_id", String.valueOf(utilisateurId));
                params.put("article_id", String.valueOf(articleId));
                Log.d("RETIRER", "Params: " + params);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void viderPanier() {
        Toast.makeText(this, "Fonction à implémenter", Toast.LENGTH_SHORT).show();
    }
}