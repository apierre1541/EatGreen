<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = $_POST['email'];

    $sql = "UPDATE users SET demande_suppression = 1 WHERE email = ?";
    $stmt = $pdo->prepare($sql);
    
    if ($stmt->execute([$email])) {
        echo json_encode(["status" => "success", "message" => "Demande de suppression envoyée à l'admin"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Échec de l'envoi de la demande"]);
    }
}
?>