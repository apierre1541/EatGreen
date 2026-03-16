package com.example.eatgreen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.Map;

public class ModifierUserActivity extends AppCompatActivity {

    private EditText etNom, etPrenom, etEmail;
    private Button btnEnregistrer;
    private int userId;
    private String IP = "10.138.3.92"; // TON IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_user);

        etNom = findViewById(R.id.etModifNom);
        etPrenom = findViewById(R.id.etModifPrenom);
        etEmail = findViewById(R.id.etModifEmail);
        btnEnregistrer = findViewById(R.id.btnEnregistrerModif);

        // On récupère les données envoyées par l'Adapter
        userId = getIntent().getIntExtra("user_id", -1);
        etNom.setText(getIntent().getStringExtra("user_nom"));
        etPrenom.setText(getIntent().getStringExtra("user_prenom"));
        etEmail.setText(getIntent().getStringExtra("user_email"));

        btnEnregistrer.setOnClickListener(v -> modifierDansLaBase());
    }

    private void modifierDansLaBase() {
        String url = "http://" + IP + "/eatgreen_api/update_user.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Toast.makeText(this, "Utilisateur mis à jour !", Toast.LENGTH_SHORT).show();
                        finish(); // Retourne à la liste
                    } else {
                        Toast.makeText(this, "Erreur : " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(userId));
                params.put("nom", etNom.getText().toString());
                params.put("prenom", etPrenom.getText().toString());
                params.put("email", etEmail.getText().toString());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }
}