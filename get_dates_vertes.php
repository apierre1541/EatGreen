<?php
require_once 'config.php';

header('Content-Type: application/json');

$response = ["success" => false, "dates" => []];

try {
    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

    if ($user_id == 0) {
        $response["message"] = "Utilisateur non spécifié";
        echo json_encode($response);
        exit();
    }

    $stmt = $pdo->prepare("SELECT jour, mois, annee FROM dates_vertes WHERE user_id = ?");
    $stmt->execute([$user_id]);
    $dates = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $response["success"] = true;
    $response["dates"] = $dates;

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>