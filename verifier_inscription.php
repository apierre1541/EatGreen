<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    $evenement_id = isset($_GET['evenement_id']) ? intval($_GET['evenement_id']) : 0;
    $user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

    if ($evenement_id == 0 || $user_id == 0) {
        echo json_encode([]);
        exit();
    }

    $stmt = $pdo->prepare("SELECT id FROM inscriptions_cours WHERE evenement_id = ? AND user_id = ?");
    $stmt->execute([$evenement_id, $user_id]);
    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode($result);

} catch(PDOException $e) {
    echo json_encode([]);
}
?>