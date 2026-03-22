<?php
require_once 'config.php';

header('Content-Type: application/json');

$response = ["success" => false, "message" => ""];

try {
    $evenement_id = isset($_POST['evenement_id']) ? intval($_POST['evenement_id']) : 0;
    $user_id = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
    $jour = isset($_POST['jour']) ? intval($_POST['jour']) : 0;
    $mois = isset($_POST['mois']) ? intval($_POST['mois']) : 0;
    $annee = isset($_POST['annee']) ? intval($_POST['annee']) : 0;

    if ($evenement_id == 0 || $user_id == 0) {
        $response["message"] = "Paramètres manquants";
        echo json_encode($response);
        exit();
    }

    // Vérifier si déjà inscrit
    $stmt = $pdo->prepare("SELECT id FROM inscriptions_cours WHERE evenement_id = ? AND user_id = ?");
    $stmt->execute([$evenement_id, $user_id]);
    
    if ($stmt->fetch()) {
        $response["message"] = "Déjà inscrit";
        echo json_encode($response);
        exit();
    }

    // Insérer l'inscription
    $stmt = $pdo->prepare("INSERT INTO inscriptions_cours (evenement_id, user_id, jour, mois, annee) VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$evenement_id, $user_id, $jour, $mois, $annee]);

    $response["success"] = true;
    $response["message"] = "Inscription réussie";

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>