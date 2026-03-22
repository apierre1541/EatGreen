<?php
require_once 'config.php';

// Récupérer les données (POST ou JSON)
$data = $_POST;
if (empty($data)) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
}

// Validation des champs requis
$required = ['date', 'heure', 'restaurant_id'];
foreach ($required as $field) {
    if (empty($data[$field])) {
        echo json_encode(["success" => false, "message" => "Champ $field requis"]);
        exit();
    }
}

$date = trim($data['date']);
$heure = trim($data['heure']);
$restaurant_id = intval($data['restaurant_id']); 

try {
    $stmt = $pdo->prepare("INSERT INTO date_heure (date, heure, restaurant_id) VALUES (?, ?, ?)");
    $stmt->execute([$date, $heure, $restaurant_id]);

    $id = $pdo->lastInsertId();

    $response = [
        "success" => true,
        "message" => "Date et heure ajoutées avec succès",
        "id" => $id,
        "restaurant_id" => $restaurant_id
    ];

    echo json_encode($response);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de l'ajout de la date et de l'heure: " . $e->getMessage()
    ]);
}
?>