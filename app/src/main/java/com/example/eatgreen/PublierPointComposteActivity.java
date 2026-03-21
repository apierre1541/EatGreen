package com.example.eatgreen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PublierPointComposteActivity extends AppCompatActivity {

    private EditText nom_point, description, latitude, longitude, adresse, code_postal, ville, type_point;
    private Button b7;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publier_point_composte);

        nom_point = findViewById(R.id.nom_point);
        description = findViewById(R.id.description);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        adresse = findViewById(R.id.adresse);
        code_postal = findViewById(R.id.code_postal);
        ville = findViewById(R.id.ville);
        type_point = findViewById(R.id.type_point);
        b7 = findViewById(R.id.b7);

        requestQueue = Volley.newRequestQueue(this);

        b7.setOnClickListener(v -> ajouterVille());
    }

    private void ajouterVille() {
        String nomPoint = nom_point.getText().toString().trim();
        String des = description.getText().toString().trim();
        String lat = latitude.getText().toString().trim();
        String lon = longitude.getText().toString().trim();
        String adr = adresse.getText().toString().trim();
        String codePostal = code_postal.getText().toString().trim();
        String vil = ville.getText().toString().trim();
        String typePoint = type_point.getText().toString().trim();

        if (nomPoint.isEmpty()) {
            nom_point.setError("Veuillez saisir le nom du point");
            nom_point.requestFocus();
            return;
        }

        if (des.isEmpty()) {
            description.setError("Veuillez saisir la description du point");
            description.requestFocus();
            return;
        }

        if (lat.isEmpty()) {
            latitude.setError("Veuillez saisir la latitude");
            latitude.requestFocus();
            return;
        }

        if (lon.isEmpty()) {
            longitude.setError("Veuillez saisir la longitude");
            longitude.requestFocus();
            return;
        }

        if (adr.isEmpty()) {
            adresse.setError("Veuillez saisir l'adresse");
            adresse.requestFocus();
            return;
        }

        if (codePostal.isEmpty()) {
            code_postal.setError("Veuillez saisir le code postal");
            code_postal.requestFocus();
            return;
        }

        if (vil.isEmpty()) {
            ville.setError("Veuillez saisir la ville");
            ville.requestFocus();
            return;
        }

        if (typePoint.isEmpty()) {
            type_point.setError("Veuillez saisir le type de point");
            type_point.requestFocus();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("nom_point", nomPoint);
        params.put("description", des);
        params.put("latitude", lat);
        params.put("longitude", lon);
        params.put("adresse", adr);
        params.put("code postal", codePostal);
        params.put("ville", vil);
        params.put("type point", typePoint);

        JSONObject jsonRequest = new JSONObject(params);

        String url = "http://192.168.1.40/eatgreen_api/ajouter_point.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(PublierPointComposteActivity.this,
                                    "Le point a était ajoutée avec succès !",
                                    Toast.LENGTH_LONG).show();

                            nom_point.setText("");
                            description.setText("");
                            latitude.setText("");
                            longitude.setText("");
                            adresse.setText("");
                            code_postal.setText("");
                            ville.setText("");
                            type_point.setText("");


                        } else {
                            Toast.makeText(PublierPointComposteActivity.this,
                                    "Erreur: " + response.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PublierPointComposteActivity.this,
                                "Erreur de parsing",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(PublierPointComposteActivity.this,
                            "Erreur réseau: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                });

        requestQueue.add(request);
    }
}