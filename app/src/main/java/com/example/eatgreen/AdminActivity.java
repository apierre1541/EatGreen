package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {

    private TextView tvWelcome, tvEmail;
    private Button btnDeconnexion, btnEditerProfil, btnGestionUtilisateurs, btnPotager, btnStats, btn, b8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        displayUserInfo();
        setupListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmail = findViewById(R.id.tvEmail);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
        btnEditerProfil = findViewById(R.id.btnEditerProfil);
        btnGestionUtilisateurs = findViewById(R.id.btnGestionUtilisateurs);
        btnPotager = findViewById(R.id.btnPotager);
        btnStats = findViewById(R.id.btnStatsGlobales);
        btn = findViewById(R.id.btn);
        b8 = findViewById(R.id.b8);
    }

    private void displayUserInfo() {
        Intent intent = getIntent();
        String nom = intent.getStringExtra("user_nom");
        String prenom = intent.getStringExtra("user_prenom");
        String email = intent.getStringExtra("user_email");

        if (nom != null && prenom != null) {
            tvWelcome.setText("Bienvenue " + prenom + " " + nom);
        } else {
            tvWelcome.setText("Bienvenue Administrateur");
        }

        if (email != null) {
            tvEmail.setText(email);
            tvEmail.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        // Redirection vers la Gestion (La page verte de ton schéma)
        btnGestionUtilisateurs.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, GestionUtilisateursActivity.class);
            startActivity(intent);
        });

        // Redirection vers l'Édition Profil
        btnEditerProfil.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ModifierUserActivity.class);
            startActivity(intent);
        });

        // Déconnexion
        btnDeconnexion.setOnClickListener(v -> logout());

        // Boutons Potager et Stats (Visuels uniquement, comme demandé)
        btnPotager.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, Publier_coursJardinageActivity.class);
            startActivity(intent);
        });
        btnStats.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, StatistiquesActivity.class);
            startActivity(intent);
        });

        btn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, ListeInscritCoursJardinageActivity.class);
            startActivity(intent);
        });

        b8.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, PublierPointComposteActivity.class);
            startActivity(intent);
        });
    }

    private void logout() {
        Toast.makeText(AdminActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}