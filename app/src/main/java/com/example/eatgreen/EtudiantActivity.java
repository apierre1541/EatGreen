package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EtudiantActivity extends AppCompatActivity {

    private TextView tvWelcome, tvEmail;
    private Button btnDeconnexion;
    // Nouveaux boutons de ta maquette
    private Button btnPreference, btnProgramme, btnCarte, btnRestaurant, btnEditerProfil ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etudiant);

        initViews();
        displayUserInfo();
        setupListeners();

    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmail = findViewById(R.id.tvEmail);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);

        // Initialisation des nouveaux boutons (assure-toi que les ID correspondent à ton XML)
        btnPreference = findViewById(R.id.btnPreference);
        btnProgramme = findViewById(R.id.btnProgramme);
        btnCarte = findViewById(R.id.btnCarte);
        btnRestaurant = findViewById(R.id.btnRestaurant);
        btnEditerProfil = findViewById(R.id.btnEditerProfil);
    }

    private void setupListeners() {
        // 1. Gestion des 4 boutons centraux
        btnPreference.setOnClickListener(v -> Toast.makeText(this, "Préférence Alimentaire", Toast.LENGTH_SHORT).show());
        btnProgramme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EtudiantActivity.this, Potager_coursJardinageActivity.class);
                startActivity(i);
            }
        });
        btnCarte.setOnClickListener(v -> Toast.makeText(this, "Carte composte", Toast.LENGTH_SHORT).show());
        btnRestaurant.setOnClickListener(v -> Toast.makeText(this, "Restaurant", Toast.LENGTH_SHORT).show());

        // 2. Gestion du bouton Editer Profil (Maquette verte)
        btnEditerProfil.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantActivity.this, EditerProfilEtudiantActivity.class);
            startActivity(intent);
        });

        // 3. Gestion de la déconnexion (Ancien code)
        btnDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }

    private void displayUserInfo() {
        Intent intent = getIntent();
        String nom = intent.getStringExtra("user_nom");
        String prenom = intent.getStringExtra("user_prenom");
        String email = intent.getStringExtra("user_email");

        if (nom != null && prenom != null) {
            tvWelcome.setText("Bienvenue " + prenom + " " + nom);
        } else {
            tvWelcome.setText("Bienvenue Étudiant");
        }

        if (email != null) {
            tvEmail.setText(email);
            tvEmail.setVisibility(View.VISIBLE);
        }
    }


    private void logout() {
        Toast.makeText(EtudiantActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(EtudiantActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}