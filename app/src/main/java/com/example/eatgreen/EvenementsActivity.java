package com.example.eatgreen;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EvenementsActivity extends AppCompatActivity {

    private LinearLayout containerEvenements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evenements);

        containerEvenements = findViewById(R.id.containerEvenements);

        String json = getIntent().getStringExtra("jsonEvenements");

        if (json == null || json.isEmpty()) {
            afficherMessage("Aucun événement reçu");
            return;
        }

        try {
            JSONArray array;
            json = json.trim();
            if (json.startsWith("[")) {
                array = new JSONArray(json);
            } else if (json.startsWith("{")) {
                JSONObject objRoot = new JSONObject(json);
                array = objRoot.getJSONArray("evenements");
            } else {
                throw new JSONException("Format JSON inattendu");
            }

            SimpleDateFormat inputTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
            SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                // Titre
                String titre = obj.optString("titre", obj.optString("title", "Titre inconnu"));

                // Construire la date depuis la bdd avec jour, mois, année
                String dateStr = obj.optString("date", "");
                if (dateStr.isEmpty()) {
                    String jour = obj.optString("jour", "01");
                    String mois = obj.optString("mois", "01");
                    String annee = obj.optString("annee", "2000");
                    dateStr = String.format("%s/%s/%s", jour, mois, annee);
                }

                // Horaire
                String horaireStr = obj.optString("horaire", obj.optString("heure", ""));
                String horaireFormatee = horaireStr;
                try {
                    Date heure = inputTimeFormat.parse(horaireStr);
                    if (heure != null) horaireFormatee = outputTimeFormat.format(heure);
                } catch (ParseException ignored) {}

                // Créer le TextView
                TextView tv = new TextView(this);
                tv.setText(dateStr + " " + horaireFormatee + " : " + titre);
                tv.setTextSize(18);
                tv.setPadding(0, 8, 0, 8);
                containerEvenements.addView(tv);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            afficherMessage("Erreur lors du parsing JSON");
        }
    }

    private void afficherMessage(String message) {
        TextView tv = new TextView(this);
        tv.setText(message);
        tv.setTextSize(18);
        tv.setPadding(0, 8, 0, 8);
        containerEvenements.addView(tv);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}