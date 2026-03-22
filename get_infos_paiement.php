<?php
require_once 'config.php';

header('Content-Type: application/json');

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

if ($user_id == 0) {
    echo json_encode(["success" => false, "message" => "ID utilisateur requis"]);
    exit();
}

try {
    $stmt = $pdo->prepare("SELECT nom, prenom, email, telephone FROM users WHERE id = ?");
    $stmt->execute([$user_id]);
    $user = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($user) {
        echo json_encode([
            "success" => true,
            "user" => $user
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Utilisateur non trouvé"]);
    }
} catch(PDOException $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>