package com.example.eatgreen;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ListeInscritCoursJardinageActivity extends AppCompatActivity {

    private LinearLayout containerInscrits;
    private TextView tvTitre;
    private RequestQueue requestQueue;
    private Button btnImprimerPDF;
    private ScrollView scrollView;
    private JSONArray donneesCours; // Pour stocker les données à imprimer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_inscrits);

        containerInscrits = findViewById(R.id.container_inscrits);
        tvTitre = findViewById(R.id.tv_titre);
        scrollView = findViewById(R.id.scrollView);
        btnImprimerPDF = findViewById(R.id.btn_imprimer_pdf);

        requestQueue = Volley.newRequestQueue(this);

        tvTitre.setText("Tous les cours et inscriptions");

        // Charger TOUS les cours sans filtre de date
        chargerTousLesCours();

        // Bouton pour imprimer en PDF
        btnImprimerPDF.setOnClickListener(v -> {
            if (donneesCours != null && donneesCours.length() > 0) {
                genererPDF();
            } else {
                Toast.makeText(this, "Aucune donnée à imprimer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Charge TOUS les cours avec leurs inscrits
     */
    private void chargerTousLesCours() {
        String url = "http://eatgreen.alwaysdata.net/eatgreen_api/get_inscrits_par_cours.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        containerInscrits.removeAllViews();
                        donneesCours = response; // Stocker pour le PDF

                        if (response.length() == 0) {
                            TextView tvVide = new TextView(ListeInscritCoursJardinageActivity.this);
                            tvVide.setText("Aucun cours disponible");
                            tvVide.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            tvVide.setTextSize(16);
                            tvVide.setPadding(0, 50, 0, 50);
                            containerInscrits.addView(tvVide);
                            return;
                        }

                        SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.FRENCH);

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject cours = response.getJSONObject(i);

                            int coursId = cours.getInt("id");
                            String titre = cours.getString("titre");
                            String horaire = cours.getString("horaire");
                            JSONArray inscrits = cours.getJSONArray("inscrits");

                            // Récupérer la date si disponible
                            String dateInfo = "";
                            if (cours.has("jour") && cours.has("mois") && cours.has("annee")) {
                                int jour = cours.getInt("jour");
                                int mois = cours.getInt("mois");
                                int annee = cours.getInt("annee");
                                dateInfo = jour + "/" + mois + "/" + annee + " - ";
                            }

                            // Formatage de l'heure
                            String heureFormatee = horaire;
                            try {
                                Date date = inputFormat.parse(horaire);
                                if (date != null) {
                                    heureFormatee = outputFormat.format(date);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            // Titre du cours avec date
                            TextView tvCoursTitre = new TextView(this);
                            tvCoursTitre.setText(dateInfo + heureFormatee + " - " + titre);
                            tvCoursTitre.setTextSize(18);
                            tvCoursTitre.setTypeface(null, android.graphics.Typeface.BOLD);
                            tvCoursTitre.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            tvCoursTitre.setPadding(0, 16, 0, 8);
                            containerInscrits.addView(tvCoursTitre);

                            if (inscrits.length() == 0) {
                                // Aucun inscrit pour ce cours
                                TextView tvAucun = new TextView(this);
                                tvAucun.setText("   Aucun inscrit");
                                tvAucun.setTextSize(14);
                                tvAucun.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                tvAucun.setPadding(16, 4, 0, 8);
                                containerInscrits.addView(tvAucun);
                            } else {
                                // Afficher le nombre d'inscrits
                                TextView tvNbInscrits = new TextView(this);
                                tvNbInscrits.setText("   " + inscrits.length() + " inscrit(s)");
                                tvNbInscrits.setTextSize(14);
                                tvNbInscrits.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                                tvNbInscrits.setPadding(16, 4, 0, 8);
                                containerInscrits.addView(tvNbInscrits);

                                // Afficher tous les inscrits pour ce cours
                                for (int j = 0; j < inscrits.length(); j++) {
                                    JSONObject inscrit = inscrits.getJSONObject(j);
                                    String nom = inscrit.getString("nom");
                                    String prenom = inscrit.getString("prenom");
                                    String email = inscrit.getString("email");

                                    View itemView = getLayoutInflater().inflate(R.layout.item_inscrit, containerInscrits, false);

                                    TextView tvNom = itemView.findViewById(R.id.tv_nom);
                                    TextView tvEmail = itemView.findViewById(R.id.tv_email);

                                    tvNom.setText("      • " + prenom + " " + nom);
                                    tvEmail.setText("        " + email);

                                    containerInscrits.addView(itemView);
                                }
                            }

                            // Ligne de séparation
                            View separator = new View(this);
                            separator.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    1
                            ));
                            separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                            separator.setPadding(0, 8, 0, 8);
                            containerInscrits.addView(separator);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ListeInscritCoursJardinageActivity.this, "Erreur de chargement: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(ListeInscritCoursJardinageActivity.this, "Erreur réseau: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        requestQueue.add(request);
    }

    /**
     * Génère un PDF à partir des données des cours
     */
    private void genererPDF() {

        if (donneesCours == null || donneesCours.length() == 0) {
            Toast.makeText(this, "Aucune donnée à imprimer", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // ===== STYLES =====
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(18);
        titlePaint.setFakeBoldText(true);

        Paint subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.BLACK);
        subtitlePaint.setTextSize(14);
        subtitlePaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(12);

        Paint smallTextPaint = new Paint();
        smallTextPaint.setColor(Color.DKGRAY);
        smallTextPaint.setTextSize(10);

        // ===== FOND =====
        canvas.drawRect(0, 0, 595, 842, backgroundPaint);

        // ===== TITRE =====
        canvas.drawText("LISTE DES INSCRIPTIONS AUX COURS", 50, 50, titlePaint);
        String dateGen = "Généré le " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(new Date());
        canvas.drawText(dateGen, 50, 70, textPaint);

        int yPosition = 100;
        int lineHeight = 25;
        int coursTraites = 0;

        try {
            for (int i = 0; i < donneesCours.length(); i++) {

                JSONObject cours = donneesCours.getJSONObject(i);

                // ✅ Vérifier espace AVANT dessin
                if (yPosition > 750) {
                    pdfDocument.finishPage(page);

                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();

                    canvas.drawRect(0, 0, 595, 842, backgroundPaint);

                    canvas.drawText("LISTE DES INSCRIPTIONS (suite)", 50, 50, titlePaint);
                    yPosition = 80;
                }

                coursTraites++;

                String titre = cours.optString("titre", "Sans titre");
                String horaire = cours.optString("horaire", "");
                JSONArray inscrits = cours.optJSONArray("inscrits");
                if (inscrits == null) inscrits = new JSONArray();

                // ===== COURS =====
                canvas.drawText(horaire + " - " + titre, 50, yPosition, subtitlePaint);
                yPosition += lineHeight;

                canvas.drawText(inscrits.length() + " inscrit(s)", 70, yPosition, textPaint);
                yPosition += lineHeight;

                // ===== INSCRITS =====
                for (int j = 0; j < inscrits.length(); j++) {

                    if (yPosition > 750) {
                        pdfDocument.finishPage(page);

                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                        page = pdfDocument.startPage(pageInfo);
                        canvas = page.getCanvas();

                        canvas.drawRect(0, 0, 595, 842, backgroundPaint);

                        canvas.drawText("LISTE DES INSCRIPTIONS (suite)", 50, 50, titlePaint);
                        yPosition = 80;
                    }

                    JSONObject inscrit = inscrits.getJSONObject(j);

                    String nom = inscrit.optString("nom", "");
                    String prenom = inscrit.optString("prenom", "");
                    String email = inscrit.optString("email", "");

                    canvas.drawText("• " + prenom + " " + nom, 90, yPosition, textPaint);
                    yPosition += lineHeight;

                    canvas.drawText(email, 110, yPosition, smallTextPaint);
                    yPosition += lineHeight;
                }

                yPosition += 15;
            }

            // ===== TOTAL =====
            if (yPosition > 750) {
                pdfDocument.finishPage(page);

                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();

                canvas.drawRect(0, 0, 595, 842, backgroundPaint);
                yPosition = 80;
            }

            canvas.drawText("Total: " + coursTraites + " cours", 50, yPosition, textPaint);

        } catch (JSONException e) {
            e.printStackTrace();
            canvas.drawText("Erreur JSON", 50, 200, textPaint);
        }

        pdfDocument.finishPage(page);

        // ===== SAUVEGARDE =====
        String fileName = "inscriptions_" + System.currentTimeMillis() + ".pdf";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);

        try {
            pdfDocument.writeTo(new FileOutputStream(file));

            Toast.makeText(this, "PDF généré: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // ===== OUVRIR PDF =====
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }
}