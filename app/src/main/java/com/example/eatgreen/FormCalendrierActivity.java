package com.example.eatgreen;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class FormCalendrierActivity extends AppCompatActivity {

    private EditText titreInput;
    private EditText heureInput;
    private TextView dateView;
    private Button btnValider;

    // L’URL de ton API PHP (localhost si sur émulateur, sinon ton serveur distant)
    private final String API_URL = "http://10.0.2.2/eatgreen_api/add_evenement.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_calendrier);

        // Récupération des vues
        titreInput = findViewById(R.id.titreInput);
        heureInput = findViewById(R.id.heureInput);
        dateView = findViewById(R.id.dateView);
        btnValider = findViewById(R.id.btnValider);

        // Limiter le titre de l'activité à 16 caractères
        titreInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});

        // Récupérer la date passée depuis le fragment
        int jour = getIntent().getIntExtra("jour", -1);
        int mois = getIntent().getIntExtra("mois", -1);
        int annee = getIntent().getIntExtra("annee", -1);

        dateView.setText(jour + "/" + mois + "/" + annee);

        // Gestion du clic sur le bouton
        btnValider.setOnClickListener(v -> {
            String titre = titreInput.getText().toString().trim();
            String horaire = heureInput.getText().toString().trim();

            if (titre.isEmpty()) {
                Toast.makeText(FormCalendrierActivity.this, "Veuillez entrer un titre", Toast.LENGTH_SHORT).show();
                return;
            }

            if (horaire.isEmpty()) {
                Toast.makeText(FormCalendrierActivity.this, "Veuillez entrer un horaire", Toast.LENGTH_SHORT).show();
                return;
            }

            // Appeler la fonction pour envoyer les données à l’API
            envoyerEvenementAPI(jour, mois, annee, titre, horaire);
        });
    }

    private void envoyerEvenementAPI(int jour, int mois, int annee, String titre, String horaire) {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "jour=" + URLEncoder.encode(String.valueOf(jour), "UTF-8") +
                        "&mois=" + URLEncoder.encode(String.valueOf(mois), "UTF-8") +
                        "&annee=" + URLEncoder.encode(String.valueOf(annee), "UTF-8") +
                        "&titre=" + URLEncoder.encode(titre, "UTF-8") +
                        "&horaire=" + URLEncoder.encode(horaire, "UTF-8");

                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                conn.disconnect();

                JSONObject jsonResponse = new JSONObject(response.toString());
                boolean success = jsonResponse.getBoolean("success");
                String message = jsonResponse.getString("message");

                runOnUiThread(() -> {
                    Toast.makeText(FormCalendrierActivity.this, message, Toast.LENGTH_SHORT).show();
                    if(success){
                        finish(); // Retour au calendrier
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(FormCalendrierActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}