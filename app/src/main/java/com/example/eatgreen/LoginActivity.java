package com.example.eatgreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private ImageView btnBack;

    private static final String BASE_URL = "http://192.168.1.40/eatgreen_api/";
    private static final String LOGIN_URL = BASE_URL + "login.php";
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Fonctionnalité à venir", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Email saisi: " + email);
        Log.d(TAG, "Mot de passe saisi: " + (password.isEmpty() ? "vide" : "non vide"));

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Connexion...");

        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("SE CONNECTER");

                        Log.d(TAG, "Réponse: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                JSONObject user = jsonObject.getJSONObject("user");
                                String role = user.getString("role");

                                Log.d(TAG, "Rôle: " + role);

                                Toast.makeText(LoginActivity.this,
                                        "Connexion réussie",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent;

                                switch (role) {
                                    case "restaurateur":
                                        intent = new Intent(LoginActivity.this, RestaurantActivity.class);
                                        // Passer les infos du restaurateur
                                        intent.putExtra("user_id", user.getInt("id"));
                                        intent.putExtra("user_nom", user.getString("nom"));
                                        intent.putExtra("user_prenom", user.getString("prenom"));
                                        intent.putExtra("user_email", user.getString("email"));

                                        // Passer les infos du restaurant si disponibles
                                        if (user.has("restaurant")) {
                                            JSONObject restaurant = user.getJSONObject("restaurant");
                                            intent.putExtra("restaurant_id", restaurant.getInt("id"));
                                            intent.putExtra("nom_restaurant", restaurant.getString("nom_restaurant"));
                                            intent.putExtra("siret", restaurant.getString("siret"));
                                            intent.putExtra("adresse", restaurant.getString("adresse"));
                                            intent.putExtra("code_postal", restaurant.getString("code_postal"));
                                            intent.putExtra("commune", restaurant.getString("commune"));
                                        }
                                        break;

                                    case "admin":
                                        intent = new Intent(LoginActivity.this, AdminActivity.class);
                                        intent.putExtra("user_id", user.getInt("id"));
                                        intent.putExtra("user_nom", user.getString("nom"));
                                        intent.putExtra("user_prenom", user.getString("prenom"));
                                        intent.putExtra("user_email", user.getString("email"));
                                        break;

                                    default: // etudiant
                                        intent = new Intent(LoginActivity.this, EtudiantActivity.class);
                                        intent.putExtra("user_id", user.getInt("id"));
                                        intent.putExtra("user_nom", user.getString("nom"));
                                        intent.putExtra("user_prenom", user.getString("prenom"));
                                        intent.putExtra("user_telephone", user.getString("telephone"));
                                        intent.putExtra("user_email", user.getString("email"));
                                        break;
                                }

                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                prefs.edit().putInt("user_id", user.getInt("id")).apply();

                                Intent i = new Intent(LoginActivity.this, AffichagePanierActivity.class);
                                intent.putExtra("UTILISATEUR_ID", user.getInt("id"));  // ← Nécessaire pour cette activité
                                startActivity(i);

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this,
                                    "Erreur de réponse", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("SE CONNECTER");

                        String message = "Erreur réseau";
                        if (error.networkResponse != null) {
                            message += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}