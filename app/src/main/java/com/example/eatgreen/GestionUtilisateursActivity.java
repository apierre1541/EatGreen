package com.example.eatgreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestionUtilisateursActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter adapter;
    private List<User> userList;
    private Button btnAjouter;

    // Configuration pour l'émulateur (10.0.2.2 pointe vers le localhost de ton PC)
    private static final String IP_ADDRESS = "10.0.2.2";
    private static final String URL_GET = "http://" + IP_ADDRESS + "/eatgreen_api/get_users.php";
    private static final String URL_DELETE = "http://" + IP_ADDRESS + "/eatgreen_api/delete_user.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_utilisateurs);

        // 1. Initialisation des vues
        rvUsers = findViewById(R.id.rvUsers);
        btnAjouter = findViewById(R.id.btnAjouterUser);

        // 2. Préparation de la liste et de l'Adapter
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, this);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        // 3. Action : Ajouter un utilisateur
        btnAjouter.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        // NOTE : On ne met pas loadUsers() ici car onResume() va s'en charger
        // automatiquement au démarrage de l'activité.
    }

    // --- CYCLE DE VIE : Rafraîchir quand on revient sur cette page ---
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // Recharge la liste à chaque fois que l'écran devient visible
    }

    // --- RÉCUPÉRATION DES DONNÉES DEPUIS LA BDD ---
    private void loadUsers() {
        // On vide la liste actuelle pour éviter les doublons lors du rafraîchissement
        userList.clear();
        adapter.notifyDataSetChanged();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL_GET, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            userList.add(new User(
                                    obj.getInt("id"),
                                    obj.getString("nom"),
                                    obj.getString("prenom"),
                                    obj.getString("email"),
                                    obj.getString("role")
                            ));
                        }
                        adapter.notifyDataSetChanged(); // Mise à jour de la vue
                    } catch (JSONException e) {
                        Log.e("ERROR_JSON", "Erreur parsing : " + e.getMessage());
                    }
                },
                error -> {
                    Log.e("ERROR_VOLLEY", "Erreur réseau : " + error.toString());
                    Toast.makeText(this, "Impossible de joindre le serveur", Toast.LENGTH_SHORT).show();
                }
        );
        // Ajout à la file d'attente de Volley
        Volley.newRequestQueue(this).add(request);
    }

    // --- SUPPRESSION (Appelée par l'Adapter via un clic sur une icône) ---
    public void supprimerUtilisateur(int userId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer")
                .setMessage("Voulez-vous vraiment supprimer cet utilisateur ?")
                .setPositiveButton("Oui", (dialog, which) -> {

                    StringRequest deleteRequest = new StringRequest(Request.Method.POST, URL_DELETE,
                            response -> {
                                // On vérifie si le PHP a renvoyé "success"
                                if (response.trim().equalsIgnoreCase("success")) {
                                    userList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    Toast.makeText(this, "Supprimé avec succès", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e("SERVER_RESPONSE", "Réponse : " + response);
                                    Toast.makeText(this, "Erreur serveur : " + response, Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(this, "Erreur réseau lors de la suppression", Toast.LENGTH_SHORT).show()
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("id", String.valueOf(userId));
                            return params;
                        }
                    };
                    Volley.newRequestQueue(this).add(deleteRequest);
                })
                .setNegativeButton("Non", null)
                .show();
    }
}