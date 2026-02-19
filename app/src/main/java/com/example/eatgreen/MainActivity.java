package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnEtudiant, btnAdmin, btnRestaurateur;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnEtudiant = findViewById(R.id.btnEtudiant);
        btnAdmin = findViewById(R.id.btnAdmin);
        btnRestaurateur = findViewById(R.id.btnRestaurateur);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        // Redirection vers RegisterActivity avec le rôle étudiant
        btnEtudiant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("role", "etudiant");
                startActivity(intent);
            }
        });

        // Redirection vers RegisterActivity avec le rôle admin
        btnAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("role", "admin");
                startActivity(intent);
            }
        });

        // Redirection vers RegisterActivity avec le rôle restaurateur
        btnRestaurateur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("role", "restaurateur");
                startActivity(intent);
            }
        });

        // Redirection vers connexion
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}