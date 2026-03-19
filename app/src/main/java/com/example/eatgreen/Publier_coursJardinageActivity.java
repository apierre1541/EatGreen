package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Publier_coursJardinageActivity extends AppCompatActivity {

    private CalendrierFragment calendrierFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publier_cours_jardinage);

        // Initialiser le fragment calendrier avec le rôle admin
        calendrierFragment = CalendrierFragment.newInstance("admin");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, calendrierFragment)
                .commit();

        // Bouton pour retourner (optionnel)
        Button btnRetour = findViewById(R.id.btn_retour);
        if (btnRetour != null) {
            btnRetour.setOnClickListener(v -> finish());
        }
    }
}