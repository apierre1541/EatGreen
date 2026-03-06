package com.example.eatgreen;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Potager_coursJardinageActivity extends AppCompatActivity {

    private EditText etDate;
    private CalendrierFragment calendrierFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_potager_cours_jardinage);

        etDate = findViewById(R.id.et_date);

        calendrierFragment = new CalendrierFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, calendrierFragment)
                .commit();

        // Écouter les changements dans l'EditText
        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String dateTexte = s.toString().trim();
                if (!dateTexte.isEmpty()) {
                    String[] parties = dateTexte.split("/");
                    if (parties.length == 3) {
                        try {
                            int jour = Integer.parseInt(parties[0]);
                            int mois = Integer.parseInt(parties[1]);
                            int annee = Integer.parseInt(parties[2]);

                            // ✅ SIMULER UN CLIC SUR LE BOUTON CORRESPONDANT
                            if (calendrierFragment != null && calendrierFragment.getView() != null) {
                                simulerClicSurJour(jour, mois, annee);
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(Potager_coursJardinageActivity.this,
                                    "Format incorrect. Utilisez JJ/MM/AAAA",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    /**
     * Simule un clic sur le bouton du jour correspondant
     */
    private void simulerClicSurJour(int jour, int mois, int annee) {
        // Accéder à la vue du fragment
        View fragmentView = calendrierFragment.getView();
        if (fragmentView == null) return;

        // Trouver le conteneur des boutons (c'est votre "container")
        // Cette partie dépend de la structure exacte de votre fragment
        LinearLayout container = fragmentView.findViewById(android.R.id.content); // À adapter !

        // Parcourir toutes les lignes et boutons pour trouver le bon
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout ligne = (LinearLayout) child;
                for (int j = 0; j < ligne.getChildCount(); j++) {
                    View bouton = ligne.getChildAt(j);
                    if (bouton instanceof EditText) {
                        // Vérifier si c'est le bon bouton (avec le bon numéro)
                        Button btn = (Button) bouton;
                        String texte = btn.getText().toString();
                        if (texte.equals(String.valueOf(jour))) {
                            // Simuler le clic
                            btn.performClick();
                            return;
                        }
                    }
                }
            }
        }
    }
}