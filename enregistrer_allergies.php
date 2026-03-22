<?php
require_once 'config.php';

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);

$user_id = isset($input['user_id']) ? intval($input['user_id']) : 0;
$allergies = isset($input['allergies']) ? $input['allergies'] : [];

if ($user_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID utilisateur invalide"]);
    exit;
}

try {
    $deleteStmt = $pdo->prepare("DELETE FROM allergies WHERE user_id = ?");
    $deleteStmt->execute([$user_id]);
 
    $insertStmt = $pdo->prepare("INSERT INTO allergies (user_id, allergie) VALUES (?, ?)");
    
    foreach ($allergies as $allergie) {
        $insertStmt->execute([$user_id, $allergie]);
    }
    
    echo json_encode(["success" => true, "message" => "Allergies enregistrées"]);
    
} catch(PDOException $e) {
    echo json_encode(["success" => false, "message" => "Erreur BDD: " . $e->getMessage()]);
}
?>