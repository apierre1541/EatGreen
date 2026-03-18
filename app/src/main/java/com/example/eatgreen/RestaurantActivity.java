package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RestaurantActivity extends AppCompatActivity {

    private TextView t1, t2, t3, t4;
    private ImageButton b2, b3, b4, b5, i1, btnDeconnexion, btnEditerProfil, btnPublierRepas;
    private Button b1;
    private int idResto; // L'ID crucial de la table 'restaurateurs'

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t3 = findViewById(R.id.t3);
        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4= findViewById(R.id.b4);
        b5 = findViewById(R.id.b5);
        i1 = findViewById(R.id.i1);
        t4 = findViewById(R.id.t4);

        displayRestaurantInfo();
        setupListeners();
    }

    private void displayRestaurantInfo() {
        Intent intent = getIntent();

        // Récupérer l'ID et le nom
        idResto = intent.getIntExtra("restaurant_id", -1);
        String nomResto = intent.getStringExtra("nom_restaurant");

        Log.d("DEBUG_EATGREEN", "ID Restaurateur reçu: " + idResto);
        Log.d("DEBUG_EATGREEN", "Nom restaurant reçu: " + nomResto);

        if (nomResto != null) {
            t2.setText("Bienvenue Restaurant " + nomResto);
        } else {
            t2.setText("Bienvenue Restaurant");
        }
    }

    private void setupListeners() {
        b1.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantActivity.this, PublierPanierActivity.class);
            intent.putExtra("restaurant_id", idResto);
            startActivity(intent);
        });

        // BOUTON DÉCONNEXION
        //btnDeconnexion.setOnClickListener(v -> {
            //Intent intent = new Intent(RestaurantActivity.this, LoginActivity.class);
          //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
           // startActivity(intent);
           // finish();
       // });

        //btnPublierRepas.setOnClickListener(v -> Toast.makeText(this, "Bientôt disponible", Toast.LENGTH_SHORT).show());
    }
}