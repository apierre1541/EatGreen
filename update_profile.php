<?php
require_once 'config.php';

header('Content-Type: application/json');

$nom = isset($_POST['nom']) ? $_POST['nom'] : '';
$prenom = isset($_POST['prenom']) ? $_POST['prenom'] : '';
$telephone = isset($_POST['telephone']) ? $_POST['telephone'] : '';
$email = isset($_POST['email']) ? $_POST['email'] : '';
$mode_paiement = isset($_POST['mode_paiement']) ? $_POST['mode_paiement'] : '';

if (empty($email)) {
    echo json_encode(["status" => "error", "message" => "Email requis"]);
    exit;
}

try {
    $sql = "UPDATE users SET 
            telephone = ?, 
            mode_paiement = ? 
            WHERE email = ?";
    
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$telephone, $mode_paiement, $email]);
    
    echo json_encode([
        "status" => "success",
        "message" => "Profil mis à jour"
    ]);
    
} catch(PDOException $e) {
    echo json_encode([
        "status" => "error",
        "message" => "Erreur BDD: " . $e->getMessage()
    ]);
}
?>