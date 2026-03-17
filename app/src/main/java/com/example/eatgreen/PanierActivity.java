package com.example.eatgreen;

import android.annotation.SuppressLint;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PanierActivity extends AppCompatActivity {

    private LinearLayout layoutPanier;
    private TextView tvTotal;
    private Button btnCommander, btnVider;
    private RequestQueue requestQueue;
    private int utilisateurId;

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

        utilisateurId = getIntent().getIntExtra("utilisateur_id", 1);

        chargerPanier();

        btnVider.setOnClickListener(v -> viderPanier());
        btnCommander.setOnClickListener(v -> validerPanier());
    }

    private void chargerPanier() {
        String url = "http://10.138.3.92/eatgreen_api/get_panier.php?utilisateur_id=" + utilisateurId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        layoutPanier.removeAllViews();

                        if (response.getBoolean("success") && response.has("articles")) {
                            JSONArray articles = response.getJSONArray("articles");

                            if (articles.length() == 0) {
                                afficherPanierVide();
                                return;
                            }

                            double totalGeneral = 0;

                            for (int i = 0; i < articles.length(); i++) {
                                JSONObject article = articles.getJSONObject(i);
                                View itemView = getLayoutInflater().inflate(R.layout.item_panier, layoutPanier, false);

                                TextView tvNom = itemView.findViewById(R.id.tv_nom_plat);
                                TextView tvQuantite = itemView.findViewById(R.id.tv_quantite);
                                TextView tvPrix = itemView.findViewById(R.id.tv_prix);
                                Button btnRetirer = itemView.findViewById(R.id.btn_retirer);

                                String nom = article.getString("nom_plat");
                                int quantite = article.getInt("quantite");
                                double prixUnitaire = article.getDouble("prix_unitaire");
                                double totalLigne = article.getDouble("total_ligne");
                                int articleId = article.getInt("article_id");

                                tvNom.setText(nom);
                                tvQuantite.setText("x" + quantite);
                                tvPrix.setText(String.format("%.2f€", totalLigne));

                                btnRetirer.setOnClickListener(v -> retirerArticle(articleId));

                                layoutPanier.addView(itemView);
                                totalGeneral += totalLigne;
                            }

                            tvTotal.setText(String.format("Total: %.2f€", totalGeneral));

                        } else {
                            afficherPanierVide();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        afficherPanierVide();
                    }
                },
                error -> {
                    Log.e("ERROR", "Erreur chargement panier: " + error.toString());
                    afficherPanierVide();
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
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

        // Utiliser une requête POST pour retirer l'article
        // (à implémenter selon votre PHP)
        chargerPanier(); // Recharger après retrait
    }

    private void viderPanier() {
        Toast.makeText(this, "Fonction à implémenter", Toast.LENGTH_SHORT).show();
    }

    private void validerPanier() {
        Toast.makeText(this, "Fonction à implémenter", Toast.LENGTH_SHORT).show();
    }
}