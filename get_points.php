<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    $stmt = $pdo->query("SELECT 
        id, 
        nom_point, 
        description, 
        latitude, 
        longitude, 
        adresse, 
        code_postal, 
        ville, 
        type_point,
        DATE_FORMAT(date_creation, '%d/%m/%Y') as date_creation
        FROM points_carte 
        ORDER BY date_creation DESC");
    
    $points = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode([
        "success" => true,
        "points" => $points
    ]);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur BDD: " . $e->getMessage()
    ]);
}
?>