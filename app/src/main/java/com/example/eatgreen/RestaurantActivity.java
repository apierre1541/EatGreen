package com.example.eatgreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RestaurantActivity extends AppCompatActivity {

    private TextView tvNomRestaurant, tvSiret, tvAdresse, tvCodePostal, tvCommune, tvNomGerant, tvEmail, tvTelephone;
    private Button btnDeconnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        initViews();
        displayRestaurantInfo();
        setupListeners();
    }

    private void initViews() {
        tvNomRestaurant = findViewById(R.id.tvNomRestaurant);
        tvSiret = findViewById(R.id.tvSiret);
        tvAdresse = findViewById(R.id.tvAdresse);
        tvCodePostal = findViewById(R.id.tvCodePostal);
        tvCommune = findViewById(R.id.tvCommune);
        tvNomGerant = findViewById(R.id.tvNomGerant);
        tvEmail = findViewById(R.id.tvEmail);
        tvTelephone = findViewById(R.id.tvTelephone);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
    }

    private void displayRestaurantInfo() {
        Intent intent = getIntent();

        // Infos du gérant
        String nom = intent.getStringExtra("user_nom");
        String prenom = intent.getStringExtra("user_prenom");
        String email = intent.getStringExtra("user_email");

        // Infos du restaurant
        String nomRestaurant = intent.getStringExtra("nom_restaurant");
        String siret = intent.getStringExtra("siret");
        String adresse = intent.getStringExtra("adresse");
        String codePostal = intent.getStringExtra("code_postal");
        String commune = intent.getStringExtra("commune");

        if (nomRestaurant == null) nomRestaurant = "Non disponible";
        if (siret == null) siret = "Non disponible";
        if (adresse == null) adresse = "Non disponible";
        if (codePostal == null) codePostal = "Non disponible";
        if (commune == null) commune = "Non disponible";

        // Afficher les informations
        tvNomGerant.setText("👤 Gérant: " + prenom + " " + nom);
        tvEmail.setText("✉️ Email: " + email);

        tvNomRestaurant.setText("🏠 " + nomRestaurant);
        tvSiret.setText("📋 SIRET: " + siret);
        tvAdresse.setText("📍 " + adresse);
        tvCodePostal.setText("📮 Code postal: " + codePostal);
        tvCommune.setText("🏘️ Commune: " + commune);
    }

    private void setupListeners() {
        btnDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        Toast.makeText(RestaurantActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RestaurantActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}