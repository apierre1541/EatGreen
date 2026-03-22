<?php
require_once 'config.php';

header('Content-Type: application/json');

ini_set('display_errors', 0);
error_reporting(0);

$response = ["success" => false, "message" => ""];

try {
    $utilisateur_id = isset($_POST['users_id']) ? intval($_POST['users_id']) : 0;
    $article_id = isset($_POST['article_id']) ? intval($_POST['article_id']) : 0;

    if ($utilisateur_id == 0 || $article_id == 0) {
        $response["message"] = "Paramètres manquants";
        echo json_encode($response);
        exit();
    }

    $stmt = $pdo->prepare("
        DELETE pa FROM panier_articles pa
        JOIN paniers p ON pa.panier_id = p.id
        WHERE pa.id = ? AND p.users_id = ? AND p.statut = 'actif'
    ");
    $stmt->execute([$article_id, $utilisateur_id]);

    if ($stmt->rowCount() > 0) {
        $response["success"] = true;
        $response["message"] = "Article retiré du panier";
    } else {
        $response["message"] = "Article non trouvé";
    }

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>