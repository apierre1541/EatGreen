<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    $restaurant_id = isset($_GET['restaurant_id']) ? intval($_GET['restaurant_id']) : 0;
    
    if ($restaurant_id == 0) {
        echo json_encode(["error" => "ID restaurant requis"]);
        exit();
    }
    
    $stmt = $pdo->prepare("SELECT id, date, heure FROM date_heure WHERE restaurant_id = ? ORDER BY date DESC, heure DESC");
    $stmt->execute([$restaurant_id]);
    $horaires = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode($horaires);
    
} catch(PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>