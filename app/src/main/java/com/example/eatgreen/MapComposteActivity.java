package com.example.eatgreen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapComposteActivity extends AppCompatActivity {

    private MapView map;
    private RequestQueue requestQueue;
    private MyLocationNewOverlay locationOverlay;
    private LocationManager locationManager;

    private List<PointData> pointsList = new ArrayList<>();
    private Button btnNearestPoint;
    private Location userLocation;

    private static class PointData {
        String nom;
        String description;
        double latitude;
        double longitude;
        String adresse;
        String codePostal;
        String ville;
        String type;
        Marker marker;
        double distance;

        PointData(String nom, String description, double latitude, double longitude,
                  String adresse, String codePostal, String ville, String type) {
            this.nom = nom;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
            this.adresse = adresse;
            this.codePostal = codePostal;
            this.ville = ville;
            this.type = type;
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE)
        );

        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        btnNearestPoint = findViewById(R.id.btn_nearest_point);

        // Cacher le TextView car vous n'en voulez plus
        TextView tvNearestInfo = findViewById(R.id.tv_nearest_info);
        tvNearestInfo.setVisibility(TextView.GONE);

        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Centre par défaut sur Limoges
        GeoPoint centerPoint = new GeoPoint(45.8367, 1.2653);
        map.getController().setZoom(14.0);
        map.getController().setCenter(centerPoint);

        setupLocationOverlay();
        chargerPointsDepuisServeur();

        btnNearestPoint.setOnClickListener(v -> trouverEtAfficherPointLePlusProche());
    }

    private void setupLocationOverlay() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (userLocation == null) {
                userLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        map.getOverlays().add(locationOverlay);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }
    }

    private void chargerPointsDepuisServeur() {
        String url = "http://192.168.1.40/eatgreen_api/get_points.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray pointsArray = response.getJSONArray("points");
                            pointsList.clear();

                            for (int i = 0; i < pointsArray.length(); i++) {
                                JSONObject obj = pointsArray.getJSONObject(i);

                                String nom = obj.getString("nom_point");
                                String description = obj.getString("description");
                                double latitude = obj.getDouble("latitude");
                                double longitude = obj.getDouble("longitude");
                                String adresse = obj.getString("adresse");
                                String codePostal = obj.getString("code_postal");
                                String ville = obj.getString("ville");
                                String type = obj.getString("type_point");

                                PointData point = new PointData(nom, description, latitude, longitude,
                                        adresse, codePostal, ville, type);
                                pointsList.add(point);

                                if (userLocation != null) {
                                    float[] results = new float[1];
                                    Location.distanceBetween(userLocation.getLatitude(),
                                            userLocation.getLongitude(),
                                            point.latitude, point.longitude, results);
                                    point.distance = results[0];
                                }

                                ajouterMarqueurSurCarte(point);
                            }

                            Toast.makeText(this,
                                    pointsList.size() + " points chargés",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(this,
                                    "Erreur: " + response.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this,
                                "Erreur de parsing",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ERROR", "Erreur réseau: " + error.toString());
                    Toast.makeText(this,
                            "Erreur de chargement: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void ajouterMarqueurSurCarte(PointData point) {
        GeoPoint geoPoint = new GeoPoint(point.latitude, point.longitude);

        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setTitle(point.nom);

        // Créer le contenu de l'infobulle (adresse + distance)
        String snippet = point.adresse + "\n" + point.codePostal + " " + point.ville;

        // Ajouter la distance si disponible
        if (userLocation != null && point.distance > 0) {
            String distanceText = formaterDistance(point.distance);
            snippet = snippet + "\n" + distanceText;
        }

        marker.setSnippet(snippet);

        // Choisir l'icône selon le type
        if (point.type.equalsIgnoreCase("Composte")) {
            marker.setTextIcon("♻️");
        } else if (point.type.equalsIgnoreCase("Restaurant")) {
            marker.setTextIcon("🍽️");
        } else if (point.type.equalsIgnoreCase("Jardin")) {
            marker.setTextIcon("🌿");
        } else {
            marker.setTextIcon("📍");
        }

        // Gestion du clic sur le marqueur
        marker.setOnMarkerClickListener((m, mapView) -> {
            // Mettre à jour la distance
            mettreAJourDistancePoint(point);
            // Mettre à jour l'infobulle
            String newSnippet = point.adresse + "\n" + point.codePostal + " " + point.ville +
                    "\n" + formaterDistance(point.distance);
            point.marker.setSnippet(newSnippet);
            // Forcer l'affichage de l'infobulle
            point.marker.showInfoWindow();
            return true; // true = on a géré le clic
        });

        point.marker = marker;
        map.getOverlays().add(marker);
    }

    private void mettreAJourDistancePoint(PointData point) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (currentLocation != null) {
                userLocation = currentLocation;
                float[] results = new float[1];
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        point.latitude, point.longitude, results);
                point.distance = results[0];
            }
        }
    }

    private String formaterDistance(double distance) {
        if (distance < 1000) {
            return String.format("Distance: %.0f mètres", distance);
        } else {
            return String.format("Distance: %.2f kilomètres", distance / 1000);
        }
    }

    private void trouverEtAfficherPointLePlusProche() {
        // Vérifier la permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission de localisation non accordée", Toast.LENGTH_LONG).show();
            return;
        }

        // Obtenir la position actuelle
        @SuppressLint("MissingPermission")
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (currentLocation == null) {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (currentLocation == null) {
            Toast.makeText(this, "Impossible d'obtenir votre position", Toast.LENGTH_LONG).show();
            return;
        }

        userLocation = currentLocation;
        double userLat = currentLocation.getLatitude();
        double userLon = currentLocation.getLongitude();

        // Calculer les distances pour tous les points
        PointData nearestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (PointData point : pointsList) {
            float[] results = new float[1];
            Location.distanceBetween(userLat, userLon, point.latitude, point.longitude, results);
            point.distance = results[0];

            if (point.distance < minDistance) {
                minDistance = point.distance;
                nearestPoint = point;
            }
        }

        if (nearestPoint != null) {
            // Mettre à jour l'infobulle du point le plus proche avec la distance
            String newSnippet = nearestPoint.adresse + "\n" + nearestPoint.codePostal + " " + nearestPoint.ville +
                    "\n" + formaterDistance(nearestPoint.distance);
            nearestPoint.marker.setSnippet(newSnippet);

            // Centrer la carte sur le point
            map.getController().setCenter(new GeoPoint(nearestPoint.latitude, nearestPoint.longitude));
            map.getController().setZoom(16.0);

            // Forcer la fermeture de toutes les infobulles avant d'ouvrir la nouvelle
            for (PointData point : pointsList) {
                if (point.marker != null && point.marker.isInfoWindowOpen()) {
                    point.marker.closeInfoWindow();
                }
            }

            // Ouvrir l'infobulle du point
            nearestPoint.marker.showInfoWindow();

            // Toast de confirmation
            Toast.makeText(this, "Point le plus proche: " + nearestPoint.nom +
                            " (" + formaterDistance(nearestPoint.distance) + ")",
                    Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Aucun point trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Localisation activée", Toast.LENGTH_SHORT).show();
                setupLocationOverlay();
                chargerPointsDepuisServeur();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
        }
    }
}