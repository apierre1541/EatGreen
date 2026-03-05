package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RestaurantActivity extends AppCompatActivity {

    private TextView tvNomRestaurant, tvSiret, tvAdresse, tvCodePostal, tvCommune, tvNomGerant, tvEmail;
    private Button btnDeconnexion, btnEditerProfil, btnPublierRepas;
    private int idResto; // L'ID crucial de la table 'restaurateurs'

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

        btnDeconnexion = findViewById(R.id.btnDeconnexion);
        btnEditerProfil = findViewById(R.id.btnEditerProfil);
        btnPublierRepas = findViewById(R.id.btnPublierRepas);
    }

    private void displayRestaurantInfo() {
        Intent intent = getIntent();

        // On récupère l'ID envoyé par LoginActivity
        idResto = intent.getIntExtra("restaurant_id", -1);
        Log.d("DEBUG_EATGREEN", "ID Restaurateur reçu: " + idResto);

        // Affichage des infos avec gestion des cas null
        tvNomGerant.setText("👤 Gérant: " + intent.getStringExtra("user_prenom") + " " + intent.getStringExtra("user_nom"));
        tvEmail.setText("✉️ Email: " + intent.getStringExtra("user_email"));

        tvNomRestaurant.setText("🏠 " + intent.getStringExtra("nom_restaurant"));
        tvSiret.setText("📋 SIRET: " + intent.getStringExtra("siret"));
        tvAdresse.setText("📍 " + intent.getStringExtra("adresse"));
        tvCodePostal.setText("📮 Code postal: " + intent.getStringExtra("code_postal"));
        tvCommune.setText("🏘️ Commune: " + intent.getStringExtra("commune"));
    }

    private void setupListeners() {
        // BOUTON MODIFIER
        btnEditerProfil.setOnClickListener(v -> {
            Intent intentEdit = new Intent(RestaurantActivity.this, ModifierRestoActivity.class);

            // On fait passer l'ID pour le WHERE de la requête SQL
            intentEdit.putExtra("ID_RESTO", idResto);

            // On passe les valeurs actuelles SANS les emojis pour le pré-remplissage
            intentEdit.putExtra("NOM_RESTO", tvNomRestaurant.getText().toString().replace("🏠 ", ""));
            intentEdit.putExtra("SIRET", tvSiret.getText().toString().replace("📋 SIRET: ", ""));
            intentEdit.putExtra("ADRESSE", tvAdresse.getText().toString().replace("📍 ", ""));
            intentEdit.putExtra("CP", tvCodePostal.getText().toString().replace("📮 Code postal: ", ""));
            intentEdit.putExtra("COMMUNE", tvCommune.getText().toString().replace("🏘️ Commune: ", ""));

            startActivity(intentEdit);
        });

        // BOUTON DÉCONNEXION
        btnDeconnexion.setOnClickListener(v -> {
            Intent intent = new Intent(RestaurantActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnPublierRepas.setOnClickListener(v -> Toast.makeText(this, "Bientôt disponible", Toast.LENGTH_SHORT).show());
    }
}