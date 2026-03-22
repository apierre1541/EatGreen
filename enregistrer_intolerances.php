<?php
require_once 'config.php';

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);

$user_id = isset($input['user_id']) ? intval($input['user_id']) : 0;
$intolerances = isset($input['intolerances']) ? $input['intolerances'] : [];

if ($user_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID utilisateur invalide"]);
    exit;
}

try {
    // Supprimer les anciennes intolérances
    $deleteStmt = $pdo->prepare("DELETE FROM intolerances WHERE user_id = ?");
    $deleteStmt->execute([$user_id]);
    
    // Insérer les nouvelles intolérances
    $insertStmt = $pdo->prepare("INSERT INTO intolerances (user_id, intolerance) VALUES (?, ?)");
    
    foreach ($intolerances as $intolerance) {
        $insertStmt->execute([$user_id, $intolerance]);
    }
    
    echo json_encode(["success" => true, "message" => "Intolérances enregistrées"]);
    
} catch(PDOException $e) {
    echo json_encode(["success" => false, "message" => "Erreur BDD: " . $e->getMessage()]);
}
?>