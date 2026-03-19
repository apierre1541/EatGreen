package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNom, etPrenom, etTelephone, etEmail, etPassword, etConfirmPassword;
    private RadioGroup radioGroupRole;
    private RadioButton radioEtudiant, radioAdmin, radioRestaurateur;
    private Button btnRegister;
    private TextView tvLogin;
    private ImageView btnBack;

    private static final String BASE_URL = "http://192.168.1.40/eatgreen_api/";
    private static final String REGISTER_URL = BASE_URL + "register.php";
    private static final String TAG = "RegisterActivity";

    // Pattern pour validation CNIL
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +         // au moins un chiffre
                    "(?=.*[a-z])" +           // au moins une minuscule
                    "(?=.*[A-Z])" +           // au moins une majuscule
                    "(?=.*[@#$%^&+=!])" +     // au moins un caractère spécial
                    "(?=\\S+$)" +              // pas d'espace
                    ".{12,}$";                 // au moins 12 caractères

    private Pattern pattern;
    private TextView tvPasswordCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pattern = Pattern.compile(PASSWORD_PATTERN);
        initViews();
        setupListeners();

        // Récupérer le rôle passé depuis MainActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("role")) {
            String role = intent.getStringExtra("role");
            preSelectRole(role);
        }
    }

    private void initViews() {
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etTelephone = findViewById(R.id.etTelephone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        radioEtudiant = findViewById(R.id.radioEtudiant);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioRestaurateur = findViewById(R.id.radioRestaurateur);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack);
    }

    private void preSelectRole(String role) {
        switch (role) {
            case "etudiant":
                radioEtudiant.setChecked(true);
                break;
            case "admin":
                radioAdmin.setChecked(true);
                break;
            case "restaurateur":
                radioRestaurateur.setChecked(true);
                break;
        }
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isValidPassword(String password) {
        return pattern.matcher(password).matches();
    }

    private String getPasswordRequirements() {
        return "Le mot de passe doit contenir :\n" +
                "• Au moins 12 caractères\n" +
                "• Une majuscule\n" +
                "• Une minuscule\n" +
                "• Un chiffre\n" +
                "• Un caractère spécial (@#$%^&+=!)";
    }

    private void registerUser() {
        // Récupérer les valeurs
        final String nom = etNom.getText().toString().trim();
        final String prenom = etPrenom.getText().toString().trim();
        final String telephone = etTelephone.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Récupérer le rôle sélectionné
        int selectedId = radioGroupRole.getCheckedRadioButtonId();
        String role = "";
        if (selectedId == R.id.radioEtudiant) {
            role = "etudiant";
        } else if (selectedId == R.id.radioAdmin) {
            role = "admin";
        } else if (selectedId == R.id.radioRestaurateur) {
            role = "restaurateur";
        }

        // Validations
        if (TextUtils.isEmpty(nom)) {
            etNom.setError("Nom requis");
            return;
        }

        if (TextUtils.isEmpty(prenom)) {
            etPrenom.setError("Prénom requis");
            return;
        }

        if (TextUtils.isEmpty(telephone)) {
            etTelephone.setError("Téléphone requis");
            return;
        }

        if (telephone.length() != 10 || !telephone.matches("[0-9]+")) {
            etTelephone.setError("Le téléphone doit contenir 10 chiffres");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            return;
        }

        // Validation spécifique selon le rôle
        if (role.equals("etudiant") && !email.endsWith("@etu.unilim.fr")) {
            etEmail.setError("Les étudiants doivent utiliser un email @etu.unilim.fr");
            return;
        }

        if (role.equals("admin") && !email.endsWith("@unilim.fr")) {
            etEmail.setError("Les administrateurs doivent utiliser un email @unilim.fr");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mot de passe requis");
            return;
        }

        // Validation CNIL du mot de passe
        if (!isValidPassword(password)) {
            etPassword.setError(getPasswordRequirements());
            Toast.makeText(this, getPasswordRequirements(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            return;
        }

        if (TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Veuillez sélectionner un rôle", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton
        btnRegister.setEnabled(false);
        btnRegister.setText("Inscription...");

        // Paramètres de la requête
        Map<String, String> params = new HashMap<>();
        params.put("nom", nom);
        params.put("prenom", prenom);
        params.put("telephone", telephone);
        params.put("email", email);
        params.put("role", role);
        params.put("mot_de_passe", password);
        params.put("confirm_mot_de_passe", confirmPassword);

        Log.d(TAG, "Paramètres: " + params.toString());

        // Requête Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("S'INSCRIRE");

                        Log.d(TAG, "Réponse: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                int userId = jsonObject.getInt("user_id");
                                String userRole = jsonObject.getString("role");

                                Toast.makeText(RegisterActivity.this,
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();

                                // Si c'est un restaurateur, rediriger vers création restaurant
                                if (userRole.equals("restaurateur")) {
                                    Intent intent = new Intent(RegisterActivity.this, CreateRestaurantActivity.class);
                                    intent.putExtra("user_id", userId);
                                    intent.putExtra("user_email", email);
                                    intent.putExtra("user_nom", nom);
                                    intent.putExtra("user_prenom", prenom);
                                    startActivity(intent);
                                } else {
                                    // Sinon, aller à la connexion
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                }
                                finish();
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this,
                                    "Erreur de réponse", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btnRegister.setEnabled(true);
                        btnRegister.setText("S'INSCRIRE");

                        String message = "Erreur réseau";
                        if (error.networkResponse != null) {
                            message += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
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