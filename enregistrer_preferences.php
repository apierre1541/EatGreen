<?php
require_once 'config.php';

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);

$user_id = isset($input['user_id']) ? intval($input['user_id']) : 0;
$vegetarien = isset($input['vegetarien']) ? $input['vegetarien'] : false;
$vegan = isset($input['vegan']) ? $input['vegan'] : false;
$a_allergies = isset($input['a_allergies']) ? $input['a_allergies'] : false;
$a_intolerances = isset($input['a_intolerances']) ? $input['a_intolerances'] : false;

if ($user_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID utilisateur invalide"]);
    exit;
}

try {
    $stmt = $pdo->prepare("INSERT INTO preferences_alimentaires (user_id, vegetarien, vegan, a_allergies, a_intolerances) 
                           VALUES (?, ?, ?, ?, ?) 
                           ON DUPLICATE KEY UPDATE 
                           vegetarien = VALUES(vegetarien), 
                           vegan = VALUES(vegan), 
                           a_allergies = VALUES(a_allergies), 
                           a_intolerances = VALUES(a_intolerances)");
    
    $stmt->execute([$user_id, $vegetarien ? 1 : 0, $vegan ? 1 : 0, $a_allergies ? 1 : 0, $a_intolerances ? 1 : 0]);
    
    echo json_encode(["success" => true, "message" => "Préférences enregistrées"]);
    
} catch(PDOException $e) {
    echo json_encode(["success" => false, "message" => "Erreur BDD: " . $e->getMessage()]);
}
?>