
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
import java.util.Iterator;
import java.util.Map;


public class PanierActivity extends AppCompatActivity {

    private LinearLayout layoutPanier;
    private TextView tvTotal;
    private Button btnCommander, btnVider;
    private RequestQueue requestQueue;
    private int restaurantId = 0;
    private int utilisateurId;
    private String nomRestaurant;

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

                // Extraire le total du TextView
                String totalStr = tvTotal.getText().toString().replace("Total: ", "").replace("€", "").trim();
                double totalValue = 0;
                try {
                    totalValue = Double.parseDouble(totalStr);
                } catch (NumberFormatException e) {
                    totalValue = 0;
                }

                Intent intent = new Intent(PanierActivity.this, CommanderActivity.class);
                intent.putExtra("restaurant_id", restaurantId);
                intent.putExtra("nom_restaurant", nomRestaurant);
                intent.putExtra("total", totalValue);
                startActivity(intent);
            }
        });
    }

    private void chargerPanier() {
        String url = "http://10.138.3.92/eatgreen_api/get_panier.php?users_id=" + utilisateurId;
        Log.d("PANIER_DEBUG", "=== DÉBUT CHARGEMENT PANIER ===");
        Log.d("PANIER_DEBUG", "URL: " + url);
        Log.d("PANIER_DEBUG", "Utilisateur ID: " + utilisateurId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("PANIER_DEBUG", "=== RÉPONSE REÇUE ===");
                    Log.d("PANIER_DEBUG", "Réponse brute: " + response.toString());

                    try {
                        layoutPanier.removeAllViews();

                        // Log de la structure de base
                        boolean success = response.getBoolean("success");
                        boolean hasArticles = response.has("articles");

                        Log.d("PANIER_DEBUG", "success: " + success);
                        Log.d("PANIER_DEBUG", "has articles: " + hasArticles);

                        if (success) {

                            JSONArray articles = response.optJSONArray("articles");

                            if (articles == null) {
                                Log.e("PANIER_DEBUG", "articles est null !");
                            } else {
                                Log.d("PANIER_DEBUG", "Nombre d'articles: " + articles.length());
                            }

                            if (articles == null || articles.length() == 0) {
                                Log.d("PANIER_DEBUG", "Panier vide");
                                afficherPanierVide();
                                return;
                            }

                            // 🔍 ANALYSE DU PREMIER ARTICLE
                            JSONObject firstArticle = articles.getJSONObject(0);
                            Log.d("PANIER_DEBUG", "=== ANALYSE DU PREMIER ARTICLE ===");
                            Log.d("PANIER_DEBUG", "Premier article complet: " + firstArticle.toString());

                            // Lister toutes les clés disponibles
                            Iterator<String> keys = firstArticle.keys();
                            Log.d("PANIER_DEBUG", "Clés disponibles dans l'article:");
                            while(keys.hasNext()) {
                                String key = keys.next();
                                Object value = firstArticle.get(key);
                                Log.d("PANIER_DEBUG", "  - " + key + " = " + value + " (type: " + value.getClass().getSimpleName() + ")");
                            }

                            // Vérification des champs importants
                            Log.d("PANIER_DEBUG", "=== VÉRIFICATION DES CHAMPS IMPORTANTS ===");

                            if (firstArticle.has("restaurant_id")) {
                                restaurantId = firstArticle.getInt("restaurant_id");
                                Log.d("PANIER_DEBUG", "✅ restaurant_id trouvé: " + restaurantId);
                            } else {
                                Log.e("PANIER_DEBUG", "❌ restaurant_id NON TROUVÉ dans le JSON !");
                            }

                            if (firstArticle.has("nom_restaurant")) {
                                nomRestaurant = firstArticle.getString("nom_restaurant");
                                Log.d("PANIER_DEBUG", "✅ nom_restaurant trouvé: " + nomRestaurant);
                            } else {
                                Log.e("PANIER_DEBUG", "❌ nom_restaurant NON TROUVÉ dans le JSON !");
                            }

                            if (firstArticle.has("plat_id")) {
                                Log.d("PANIER_DEBUG", "✅ plat_id trouvé: " + firstArticle.getInt("plat_id"));
                            } else {
                                Log.e("PANIER_DEBUG", "❌ plat_id NON TROUVÉ !");
                            }

                            if (firstArticle.has("quantite")) {
                                Log.d("PANIER_DEBUG", "✅ quantite trouvé: " + firstArticle.getInt("quantite"));
                            } else {
                                Log.e("PANIER_DEBUG", "❌ quantite NON TROUVÉ !");
                            }

                            double totalGeneral = 0;

                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject article = articles.getJSONObject(i);
                                Log.d("PANIER_DEBUG", "Traitement article " + i);

                                View itemView = getLayoutInflater().inflate(R.layout.item_panier, layoutPanier, false);

                                TextView tvNom = itemView.findViewById(R.id.tv_nom_plat);
                                TextView tvQuantite = itemView.findViewById(R.id.tv_quantite);
                                TextView tvPrix = itemView.findViewById(R.id.tv_prix);
                                Button btnRetirer = itemView.findViewById(R.id.btn_retirer);

                                // Récupération avec logs
                                String nom = article.optString("nom_plat", "Inconnu");
                                int quantite = article.optInt("quantite", 0);
                                double totalLigne = article.optDouble("total_ligne", 0);
                                int articleId = article.optInt("article_id", 0);

                                Log.d("PANIER_DEBUG", "Article " + i + " - nom: " + nom + ", quantite: " + quantite + ", total: " + totalLigne + ", id: " + articleId);

                                tvNom.setText(nom);
                                tvQuantite.setText("x" + quantite);
                                tvPrix.setText(String.format("%.2f€", totalLigne));

                                final int finalArticleId = articleId;
                                btnRetirer.setOnClickListener(v -> {
                                    Log.d("PANIER_DEBUG", "Clic sur retirer pour article ID: " + finalArticleId);
                                    retirerArticle(finalArticleId);
                                });

                                layoutPanier.addView(itemView);
                                totalGeneral += totalLigne;
                            }

                            Log.d("PANIER_DEBUG", "Total général calculé: " + totalGeneral);
                            tvTotal.setText(String.format("Total: %.2f€", totalGeneral));

                            Log.d("PANIER_DEBUG", "=== FIN CHARGEMENT PANIER - SUCCÈS ===");
                            Log.d("PANIER_DEBUG", "restaurantId final: " + restaurantId);
                            Log.d("PANIER_DEBUG", "nomRestaurant final: " + nomRestaurant);

                        } else {
                            String message = response.optString("message", "Erreur inconnue");
                            Log.e("PANIER_DEBUG", "success=false, message: " + message);
                            afficherPanierVide();
                        }

                    } catch (JSONException e) {
                        Log.e("PANIER_DEBUG", "❌ ERREUR JSON: " + e.getMessage());
                        Log.e("PANIER_DEBUG", "Stack trace: ", e);
                        e.printStackTrace();
                        afficherPanierVide();
                    }
                },
                error -> {
                    Log.e("PANIER_DEBUG", "❌ ERREUR RÉSEAU");
                    Log.e("PANIER_DEBUG", "Type d'erreur: " + error.getClass().getSimpleName());
                    if (error.networkResponse != null) {
                        Log.e("PANIER_DEBUG", "Code HTTP: " + error.networkResponse.statusCode);
                        Log.e("PANIER_DEBUG", "Réponse: " + new String(error.networkResponse.data));
                    }
                    Log.e("PANIER_DEBUG", "Message: " + error.getMessage());
                    afficherPanierVide();
                    Toast.makeText(this, "Erreur de chargement: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void afficherPanierVide() {
        layoutPanier.removeAllViews();
        TextView tvVide = new TextView(this);
        tvVide.setText("Votre panier est vide");
        tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvVide.setTextSize(18);
        tvVide.setPadding(0, 50, 0, 50);
        layoutPanier.addView(tvVide);
        tvTotal.setText("Total: 0.00€");
    }

    private void retirerArticle(int articleId) {
        String url = "http://10.138.3.92/eatgreen_api/retirer_panier.php";

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

    private void validerPanier() {
        Toast.makeText(this, "Fonction à implémenter", Toast.LENGTH_SHORT).show();
    }
}
