package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaypalActivity extends AppCompatActivity {

    private EditText etEmailPaypal;
    private Button btnEnregistrer;
    private static final String SAVE_PAYPAL_URL = "http://10.0.2.2/eatgreen_api/save_paypal.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal);

        etEmailPaypal = findViewById(R.id.etEmailPaypal);
        btnEnregistrer = findViewById(R.id.btnEnregistrerPaypal);

        btnEnregistrer.setOnClickListener(v -> {
            String emailPaypal = etEmailPaypal.getText().toString().trim();

            if (emailPaypal.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailPaypal).matches()) {
                etEmailPaypal.setError("Veuillez entrer un email valide");
            } else {
                savePaypalToDatabase();
            }
        });
    }

    private void savePaypalToDatabase() {
        // Récupérer l'email de l'utilisateur passé par la page précédente
        final String emailUser = getIntent().getStringExtra("email");
        final String emailPaypal = etEmailPaypal.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, SAVE_PAYPAL_URL,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "Compte PayPal lié !", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, EtudiantActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", emailUser);
                params.put("paypal_email", emailPaypal);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}