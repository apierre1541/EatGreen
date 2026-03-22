package com.example.eatgreen;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntolerancesActivity extends AppCompatActivity {

    private CheckBox cbLactose, cbGluten, cbFructose, cbHistamine;
    private EditText etAutre;
    private Button btnEnregistrer;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private int userId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intolerances);

        cbLactose = findViewById(R.id.intolerance_lactose);
        cbGluten = findViewById(R.id.intolerance_gluten);
        cbFructose = findViewById(R.id.intolerance_fructose);
        cbHistamine = findViewById(R.id.intolerance_histamine);
        etAutre = findViewById(R.id.autre_intolerance);
        btnEnregistrer = findViewById(R.id.btn_enregistrer_intolerances);

        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("preferences_alimentaires", MODE_PRIVATE);

        chargerIntolerances();

        btnEnregistrer.setOnClickListener(v -> enregistrerIntolerances());
    }

    private void chargerIntolerances() {
        cbLactose.setChecked(sharedPreferences.getBoolean("intolerance_lactose", false));
        cbGluten.setChecked(sharedPreferences.getBoolean("intolerance_gluten", false));
        cbFructose.setChecked(sharedPreferences.getBoolean("intolerance_fructose", false));
        cbHistamine.setChecked(sharedPreferences.getBoolean("intolerance_histamine", false));
        etAutre.setText(sharedPreferences.getString("intolerance_autre", ""));
    }

    private void enregistrerIntolerances() {
        List<String> intolerances = new ArrayList<>();

        if (cbLactose.isChecked()) intolerances.add("Lactose");
        if (cbGluten.isChecked()) intolerances.add("Gluten");
        if (cbFructose.isChecked()) intolerances.add("Fructose");
        if (cbHistamine.isChecked()) intolerances.add("Histamine");

        String autre = etAutre.getText().toString().trim();
        if (!autre.isEmpty()) {
            intolerances.add(autre);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("intolerance_lactose", cbLactose.isChecked());
        editor.putBoolean("intolerance_gluten", cbGluten.isChecked());
        editor.putBoolean("intolerance_fructose", cbFructose.isChecked());
        editor.putBoolean("intolerance_histamine", cbHistamine.isChecked());
        editor.putString("intolerance_autre", autre);
        editor.apply();

        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/enregistrer_intolerances.php";

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("intolerances", intolerances);

        JSONObject jsonRequest = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Intolérances enregistrées", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Erreur: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        requestQueue.add(request);
    }
}