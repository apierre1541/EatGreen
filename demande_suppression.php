<?php
include 'db.php'; // On se connecte à la BDD

if($_SERVER['REQUEST_METHOD'] == 'POST'){
    $email = $_POST['email'];

    // On ne supprime pas direct, on met à jour un statut pour l'admin
    $sql = "UPDATE users SET demande_suppression = 1 WHERE email = ?";
    $stmt = $pdo->prepare($sql);
    
    if($stmt->execute([$email])){
        echo json_encode(["result" => "success", "message" => "Demande envoyée"]);
    } else {
        echo json_encode(["result" => "error", "message" => "Erreur BDD"]);
    }
}
?>