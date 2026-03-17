package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DateHeureActivity extends AppCompatActivity {

    EditText date, heure;
    Button b9;
    RequestQueue requestQueue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_heure);

        date = findViewById(R.id.date);
        heure = findViewById(R.id.heure);
        b9 = findViewById(R.id.b9);

        requestQueue = Volley.newRequestQueue(this);

        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                envoyerDonnees();
            }
        });
    }

    private void envoyerDonnees() {
        String dateTexte = date.getText().toString().trim();
        String heureTexte = heure.getText().toString().trim();

        if (dateTexte.isEmpty() || heureTexte.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.138.3.92/eatgreen_api/date_heure.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(DateHeureActivity.this,
                                        jsonResponse.getString("message"),
                                        Toast.LENGTH_LONG).show();

                                date.setText("");
                                heure.setText("");

                                // Optionnel: focus sur le premier champ
                                date.requestFocus();

                            } else {
                                Toast.makeText(DateHeureActivity.this,
                                        jsonResponse.getString("message"),
                                        Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DateHeureActivity.this,
                                    "Erreur de parsing", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DateHeureActivity.this,
                                "Erreur réseau: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("date", date.getText().toString().trim());
                params.put("heure", heure.getText().toString().trim());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}