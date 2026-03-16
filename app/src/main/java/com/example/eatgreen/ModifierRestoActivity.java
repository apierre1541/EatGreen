package com.example.eatgreen;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class ModifierRestoActivity extends AppCompatActivity {

    private EditText etNom, etSiret, etAdresse, etCP, etCommune;
    private Button btnSave;
    private int restoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_resto);

        initViews();

        // Récupération de l'ID et des données
        restoId = getIntent().getIntExtra("ID_RESTO", -1);

        if (restoId == -1) {
            Toast.makeText(this, "Erreur critique: ID introuvable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        preRemplirChamps();

        btnSave.setOnClickListener(v -> envoyerModifications());
    }

    private void initViews() {
        etNom = findViewById(R.id.etNomResto);
        etSiret = findViewById(R.id.etSiret);
        etAdresse = findViewById(R.id.etAdresse);
        etCP = findViewById(R.id.etCP);
        etCommune = findViewById(R.id.etCommune);
        btnSave = findViewById(R.id.btnEnregistrerResto);
    }

    private void preRemplirChamps() {
        etNom.setText(getIntent().getStringExtra("NOM_RESTO"));
        etSiret.setText(getIntent().getStringExtra("SIRET"));
        etAdresse.setText(getIntent().getStringExtra("ADRESSE"));
        etCP.setText(getIntent().getStringExtra("CP"));
        etCommune.setText(getIntent().getStringExtra("COMMUNE"));
    }

    private void envoyerModifications() {
        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/update_restaurateur.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Profil mis à jour avec succès !", Toast.LENGTH_SHORT).show();
                        finish(); // Retour à l'accueil
                    } else {
                        Toast.makeText(this, "Erreur: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau (vérifiez WAMP)", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Clés identiques à ton PHP ($_POST['id'], etc.)
                params.put("id", String.valueOf(restoId));
                params.put("nom_restaurant", etNom.getText().toString().trim());
                params.put("siret", etSiret.getText().toString().trim());
                params.put("adresse", etAdresse.getText().toString().trim());
                params.put("code_postal", etCP.getText().toString().trim());
                params.put("commune", etCommune.getText().toString().trim());
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}