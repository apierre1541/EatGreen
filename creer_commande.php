<?php
require_once 'config.php';

header('Content-Type: application/json');

// Récupérer les données POST
$utilisateur_id = isset($_POST['utilisateur_id']) ? intval($_POST['utilisateur_id']) : 0;
$email = isset($_POST['email']) ? $_POST['email'] : '';
$restaurant_id = isset($_POST['restaurant_id']) ? intval($_POST['restaurant_id']) : 0;
$horaire_id = isset($_POST['horaire_id']) ? intval($_POST['horaire_id']) : 0;
$date = isset($_POST['date']) ? $_POST['date'] : '';
$heure = isset($_POST['heure']) ? $_POST['heure'] : '';
$mode_paiement = isset($_POST['mode_paiement']) ? $_POST['mode_paiement'] : '';
$total = isset($_POST['total']) ? floatval($_POST['total']) : 0;
$statut = isset($_POST['statut']) ? $_POST['statut'] : 'finalise';

// Validation
if ($utilisateur_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID utilisateur invalide"]);
    exit;
}

if ($restaurant_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID restaurant invalide"]);
    exit;
}

if ($horaire_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID horaire invalide"]);
    exit;
}

try {
    // Vérifier si l'utilisateur existe
    $userStmt = $pdo->prepare("SELECT id FROM users WHERE id = ? OR email = ?");
    $userStmt->execute([$utilisateur_id, $email]);
    if ($userStmt->rowCount() == 0) {
        echo json_encode(["success" => false, "message" => "Utilisateur non trouvé"]);
        exit;
    }
    
    // Vérifier si le créneau existe dans la table date_heure
    $horaireStmt = $pdo->prepare("SELECT id FROM date_heure WHERE id = ?");
    $horaireStmt->execute([$horaire_id]);
    $horaire = $horaireStmt->fetch();
    
    if (!$horaire) {
        echo json_encode(["success" => false, "message" => "Créneau horaire non trouvé"]);
        exit;
    }
    
    // Insérer la commande
    $stmt = $pdo->prepare("INSERT INTO commandes 
        (utilisateur_id, restaurant_id, horaire_id, date_commande, heure, mode_paiement, total, statut, date_creation) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())");
    
    $result = $stmt->execute([$utilisateur_id, $restaurant_id, $horaire_id, $date, $heure, $mode_paiement, $total, $statut]);
    
    if ($result) {
        $commande_id = $pdo->lastInsertId();
        
        echo json_encode([
            "success" => true,
            "message" => "Commande finalisée avec succès",
            "commande_id" => $commande_id
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Erreur lors de l'insertion"]);
    }
    
} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur BDD: " . $e->getMessage()
    ]);
}
?>