<?php
require_once 'config.php';

// Désactiver l'affichage des erreurs qui polluent le JSON
ini_set('display_errors', 0);
error_reporting(0);

header('Content-Type: application/json');

$response = ["success" => false, "message" => ""];

try {
    // ✅ Récupérer les données texte (POST normaux)
    $nom_plat = isset($_POST['nom_plat']) ? trim($_POST['nom_plat']) : '';
    $condition_repas = isset($_POST['condition_repas']) ? trim($_POST['condition_repas']) : '';
    $quantite = isset($_POST['quantite']) ? trim($_POST['quantite']) : '';
    $prix = isset($_POST['prix']) ? trim($_POST['prix']) : '';
    $adresse_postal = isset($_POST['adresse_postal']) ? trim($_POST['adresse_postal']) : '';
    $code_postal = isset($_POST['code_postal']) ? trim($_POST['code_postal']) : '';
    $commune = isset($_POST['commune']) ? trim($_POST['commune']) : '';
    $restaurant_id = isset($_POST['restaurant_id']) ? intval($_POST['restaurant_id']) : 0;  // ← AJOUTÉ

    // Validation des champs requis
    if (empty($nom_plat) || empty($condition_repas) || empty($quantite) || empty($prix) || 
        empty($adresse_postal) || empty($code_postal) || empty($commune) || $restaurant_id == 0) {  // ← MODIFIÉ
        $response["message"] = "Tous les champs sont requis";
        echo json_encode($response);
        exit();
    }

    // GESTION DE LA PHOTO (upload de fichier)
    $photo_name = "";
    if (isset($_FILES['photo']) && $_FILES['photo']['error'] == 0) {
        // Créer le dossier s'il n'existe pas
        $upload_dir = 'uploads/';
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        $extension = pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION);
        $photo_name = time() . '_' . uniqid() . '.' . $extension;
        $upload_path = $upload_dir . $photo_name;
        
        if (!move_uploaded_file($_FILES['photo']['tmp_name'], $upload_path)) {
            $response["message"] = "Erreur lors de l'upload de l'image";
            echo json_encode($response);
            exit();
        }
    }

    // ✅ REQUÊTE AVEC RESTAURANT_ID
    $stmt = $pdo->prepare("INSERT INTO panier_repas (nom_plat, photo, condition_repas, quantite, prix, adresse_postal, code_postal, commune, restaurant_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    $stmt->execute([$nom_plat, $photo_name, $condition_repas, $quantite, $prix, $adresse_postal, $code_postal, $commune, $restaurant_id]);

    $id = $pdo->lastInsertId();

    $response["success"] = true;
    $response["message"] = "Plat ajouté avec succès";
    $response["id"] = $id;
    $response["photo"] = $photo_name;
    $response["restaurant_id"] = $restaurant_id;  // ← AJOUTÉ

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
} catch(Exception $e) {
    $response["message"] = "Erreur: " . $e->getMessage();
}

echo json_encode($response);
?>