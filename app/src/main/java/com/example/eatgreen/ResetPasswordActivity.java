package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.regex.Pattern;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword, etConfirmPassword;
    private Button btnReset;

    private String token;

    private static final String BASE_URL = "http://10.0.2.2/eatgreen_api/";
    private static final String RESET_PASSWORD_URL = BASE_URL + "reset_password.php";
    private static final String TAG = "ResetPasswordActivity";

    // Pattern pour validation CNIL
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])" +
                    "(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=.*[@#$%^&+=!])" +
                    "(?=\\S+$)" +
                    ".{12,}$";

    private Pattern pattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        pattern = Pattern.compile(PASSWORD_PATTERN);

        // Récupérer les données de l'intent
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        token = intent.getStringExtra("token");

        initViews();

        // Pré-remplir l'email
        if (email != null) {
            etEmail.setText(email);
        }

        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnReset = findViewById(R.id.btnReset);
    }

    private void setupListeners() {
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
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

    private void resetPassword() {
        final String email = etEmail.getText().toString().trim();
        final String newPassword = etNewPassword.getText().toString().trim();
        final String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Mot de passe requis");
            return;
        }

        if (!isValidPassword(newPassword)) {
            etNewPassword.setError(getPasswordRequirements());
            Toast.makeText(this, getPasswordRequirements(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Les mots de passe ne correspondent pas");
            return;
        }

        btnReset.setEnabled(false);
        btnReset.setText("Réinitialisation...");

        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("token", token);
        params.put("new_password", newPassword);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, RESET_PASSWORD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        btnReset.setEnabled(true);
                        btnReset.setText("RÉINITIALISER");

                        Log.d(TAG, "Réponse: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                Toast.makeText(ResetPasswordActivity.this,
                                        jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();

                                // Rediriger vers la connexion
                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ResetPasswordActivity.this,
                                    "Erreur de réponse", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        btnReset.setEnabled(true);
                        btnReset.setText("RÉINITIALISER");

                        String message = "Erreur réseau";
                        if (error.networkResponse != null) {
                            message += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
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