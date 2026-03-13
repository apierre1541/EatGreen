package com.example.eatgreen;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;

public class StatistiquesActivity extends AppCompatActivity {

    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiques);

        pieChart = findViewById(R.id.pieChart);

        // A remplacer par des requetes SQL pour récupérer les données
        int nonInscrits = getDonneesDepuisBDD("Non inscrits");
        int jardinage = getDonneesDepuisBDD("Jardinage");
        int paniers = getDonneesDepuisBDD("Paniers");
        int autres = getDonneesDepuisBDD("Autres");

        // Création du Camambert
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(nonInscrits, "Non inscrits"));
        entries.add(new PieEntry(jardinage, "Jardinage"));
        entries.add(new PieEntry(paniers, "Paniers"));
        entries.add(new PieEntry(autres, "Autres"));

        // Couleur Camambert
        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] couleurs = {
                Color.parseColor("#F44336"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#2196F3")
        };
        dataSet.setColors(couleurs);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);

        // Configuration du PieChart
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setCenterText("");
        pieChart.setDrawCenterText(false);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private int getDonneesDepuisBDD(String categorie) {
        switch (categorie) {
            case "Non inscrits": return 35;
            case "Jardinage": return 25;
            case "Paniers": return 30;
            case "Autres": return 10;
            default: return 0;
        }
    }
}