<?php
// Inclusion de la connexion à la base de données (ton fichier db.php)
include 'db.php';

// On vérifie que les données sont envoyées en POST
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    
    // On récupère les infos envoyées par l'appli Android
    $email = $_POST['email']; // L'email sert d'identifiant pour savoir qui modifier
    $nom = $_POST['nom'];
    $prenom = $_POST['prenom'];
    $telephone = $_POST['telephone'];
    $mode_paiement = $_POST['mode_paiement'];

    try {
        // Préparation de la requête SQL de mise à jour
        $sql = "UPDATE users 
                SET nom = ?, prenom = ?, telephone = ?, mode_paiement = ? 
                WHERE email = ?";
        
        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([$nom, $prenom, $telephone, $mode_paiement, $email]);

        if ($result) {
            echo json_encode(["status" => "success", "message" => "Profil mis à jour avec succès"]);
        } else {
            echo json_encode(["status" => "error", "message" => "Erreur lors de la mise à jour"]);
        }
    } catch (PDOException $e) {
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }
}
?>