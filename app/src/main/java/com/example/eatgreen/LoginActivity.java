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

    private static final String BASE_URL = "http://eatgreen.alwaysdata.net/eatgreen_api/";
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
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                                int userId = user.getInt("id");
                                String userEmail = user.getString("email");
                                String userNom = user.getString("nom");
                                String userPrenom = user.getString("prenom");
                                String userTelephone = user.optString("telephone", "");

                                Log.d(TAG, "Rôle: " + role);
                                Log.d(TAG, "ID utilisateur: " + userId);
                                Log.d(TAG, "Email utilisateur: " + userEmail);

                                Toast.makeText(LoginActivity.this,
                                        "Connexion réussie",
                                        Toast.LENGTH_SHORT).show();

                                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("user_id", userId);
                                editor.putString("user_email", userEmail);
                                editor.putString("user_nom", userNom);
                                editor.putString("user_prenom", userPrenom);
                                editor.putString("user_telephone", userTelephone);
                                editor.apply();

                                // VÉRIFICATION IMMÉDIATE
                                int savedId = prefs.getInt("user_id", 0);
                                String savedEmail = prefs.getString("user_email", "");
                                Log.d(TAG, "=== SAUVEGARDE VÉRIFIÉE ===");
                                Log.d(TAG, "ID sauvegardé: " + savedId);
                                Log.d(TAG, "Email sauvegardé: " + savedEmail);
                                Log.d(TAG, "=========================");

                                // Créer l'intent pour AffichagePanierActivity
                                Intent panierIntent = new Intent(LoginActivity.this, AffichagePanierActivity.class);
                                panierIntent.putExtra("UTILISATEUR_ID", userId);
                                startActivity(panierIntent);

                                // Créer l'intent principal selon le rôle
                                Intent intent;
                                switch (role) {
                                    case "restaurateur":
                                        intent = new Intent(LoginActivity.this, RestaurantActivity.class);
                                        intent.putExtra("user_id", userId);
                                        intent.putExtra("user_nom", userNom);
                                        intent.putExtra("user_prenom", userPrenom);
                                        intent.putExtra("user_email", userEmail);

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
                                        intent.putExtra("user_id", userId);
                                        intent.putExtra("user_nom", userNom);
                                        intent.putExtra("user_prenom", userPrenom);
                                        intent.putExtra("user_email", userEmail);
                                        break;

                                    default: // etudiant
                                        intent = new Intent(LoginActivity.this, EtudiantActivity.class);
                                        intent.putExtra("user_id", userId);
                                        intent.putExtra("user_nom", userNom);
                                        intent.putExtra("user_prenom", userPrenom);
                                        intent.putExtra("user_telephone", userTelephone);
                                        intent.putExtra("user_email", userEmail);
                                        break;
                                }

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