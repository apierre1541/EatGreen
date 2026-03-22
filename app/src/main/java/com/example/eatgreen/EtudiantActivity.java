package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EtudiantActivity extends AppCompatActivity {

    private String nomEtudiant;
    private String prenomEtudiant;
    private String telephoneEtudiant;
    private String emailEtudiant;
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
        btnPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EtudiantActivity.this, PreferencesAlimentairesActivity.class);
                startActivity(i);
            }
        });
        btnProgramme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EtudiantActivity.this, Potager_coursJardinageActivity.class);
                startActivity(i);
            }
        });
        btnCarte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EtudiantActivity.this, MapComposteActivity.class);
                startActivity(i);
            }
        });
        btnRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EtudiantActivity.this, AffichagePanierActivity.class);
                startActivity(i);
            }
        });

        // 2. Gestion du bouton Editer Profil (Maquette verte)
        btnEditerProfil.setOnClickListener(v -> {
            Intent intent = new Intent(EtudiantActivity.this, EditerProfilEtudiantActivity.class);
            intent.putExtra("user_nom", nomEtudiant);
            intent.putExtra("user_prenom", prenomEtudiant);
            intent.putExtra("user_telephone", telephoneEtudiant);
            intent.putExtra("user_email", emailEtudiant);

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
        nomEtudiant = intent.getStringExtra("user_nom");
        prenomEtudiant = intent.getStringExtra("user_prenom");
        telephoneEtudiant = intent.getStringExtra("user_telephone");
        emailEtudiant = intent.getStringExtra("user_email");

        // Afficher dans les TextView
        if (nomEtudiant != null && prenomEtudiant != null) {
            tvWelcome.setText("Bienvenue " + prenomEtudiant + " " + nomEtudiant);
        } else {
            tvWelcome.setText("Bienvenue Étudiant");
        }

        if (emailEtudiant != null) {
            tvEmail.setText(emailEtudiant);
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