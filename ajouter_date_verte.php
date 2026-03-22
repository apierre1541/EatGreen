<?php
require_once 'config.php';

header('Content-Type: application/json');

$response = ["success" => false, "message" => ""];

try {
    $user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    $jour = isset($_POST['jour']) ? intval($_POST['jour']) : 0;
    $mois = isset($_POST['mois']) ? intval($_POST['mois']) : 0;
    $annee = isset($_POST['annee']) ? intval($_POST['annee']) : 0;

    if ($user_id == 0 || $jour == 0 || $mois == 0 || $annee == 0) {
        $response["message"] = "Paramètres manquants";
        echo json_encode($response);
        exit();
    }

    // Vérifier si déjà existant
    $stmt = $pdo->prepare("SELECT id FROM dates_vertes WHERE user_id = ? AND jour = ? AND mois = ? AND annee = ?");
    $stmt->execute([$user_id, $jour, $mois, $annee]);
    
    if ($stmt->fetch()) {
        // Déjà existant, on supprime (toggle)
        $stmt = $pdo->prepare("DELETE FROM dates_vertes WHERE user_id = ? AND jour = ? AND mois = ? AND annee = ?");
        $stmt->execute([$user_id, $jour, $mois, $annee]);
        $response["message"] = "Date retirée des favoris";
        $response["est_vert"] = false;
    } else {
        // Ajouter
        $date_complete = sprintf("%04d-%02d-%02d", $annee, $mois, $jour);
        $stmt = $pdo->prepare("INSERT INTO dates_vertes (user_id, jour, mois, annee, date_complete) VALUES (?, ?, ?, ?, ?)");
        $stmt->execute([$user_id, $jour, $mois, $annee, $date_complete]);
        $response["message"] = "Date marquée en vert";
        $response["est_vert"] = true;
    }

    $response["success"] = true;

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>