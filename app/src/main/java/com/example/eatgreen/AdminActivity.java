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
    private Button btnDeconnexion;

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
        btnDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
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