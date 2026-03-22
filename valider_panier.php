<?php
require_once 'config.php';

header('Content-Type: application/json');

ini_set('display_errors', 0);
error_reporting(0);

$response = ["success" => false, "message" => ""];

try {
    $utilisateur_id = isset($_POST['users_id']) ? intval($_POST['users_id']) : 0;

    if ($utilisateur_id == 0) {
        $response["message"] = "Utilisateur non spécifié";
        echo json_encode($response);
        exit();
    }

    // Mettre à jour le statut du panier
    $stmt = $pdo->prepare("UPDATE paniers SET statut = 'valide' WHERE users_id = ? AND statut = 'actif'");
    $stmt->execute([$utilisateur_id]);

    if ($stmt->rowCount() > 0) {
        $response["success"] = true;
        $response["message"] = "Panier validé avec succès";
    } else {
        $response["message"] = "Aucun panier actif trouvé";
    }

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>