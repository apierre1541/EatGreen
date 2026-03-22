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

// AFFICHER LES DONNÉES REÇUES POUR DÉBOGUER
$debug = [
    "utilisateur_id" => $utilisateur_id,
    "email" => $email,
    "restaurant_id" => $restaurant_id,
    "horaire_id" => $horaire_id,
    "date" => $date,
    "heure" => $heure,
    "mode_paiement" => $mode_paiement,
    "total" => $total
];

// Validation
if ($utilisateur_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID utilisateur invalide", "debug" => $debug]);
    exit;
}

if ($restaurant_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID restaurant invalide", "debug" => $debug]);
    exit;
}

if ($horaire_id <= 0) {
    echo json_encode(["success" => false, "message" => "ID horaire invalide", "debug" => $debug]);
    exit;
}

try {
    // Vérifier si la table commandes existe
    $tableCheck = $pdo->query("SHOW TABLES LIKE 'commandes'");
    if ($tableCheck->rowCount() == 0) {
        echo json_encode(["success" => false, "message" => "La table 'commandes' n'existe pas"]);
        exit;
    }
    
    // Vérifier si l'utilisateur existe
    $userStmt = $pdo->prepare("SELECT id FROM users WHERE id = ? OR email = ?");
    $userStmt->execute([$utilisateur_id, $email]);
    if ($userStmt->rowCount() == 0) {
        echo json_encode(["success" => false, "message" => "Utilisateur non trouvé", "debug" => $debug]);
        exit;
    }
    
    // Vérifier si l'horaire existe
    $horaireStmt = $pdo->prepare("SELECT id, places_disponibles FROM horaires WHERE id = ?");
    $horaireStmt->execute([$horaire_id]);
    $horaire = $horaireStmt->fetch();
    
    if (!$horaire) {
        echo json_encode(["success" => false, "message" => "Horaire non trouvé", "debug" => $debug]);
        exit;
    }
    
    // Insérer la commande
    $stmt = $pdo->prepare("INSERT INTO commandes 
        (utilisateur_id, restaurant_id, horaire_id, date_commande, heure, mode_paiement, total, statut, date_creation) 
        VALUES (?, ?, ?, ?, ?, ?, ?, 'finalise', NOW())");
    
    $result = $stmt->execute([$utilisateur_id, $restaurant_id, $horaire_id, $date, $heure, $mode_paiement, $total]);
    
    if ($result) {
        $commande_id = $pdo->lastInsertId();
        
        // Diminuer les places disponibles (si la colonne existe)
        try {
            $updateStmt = $pdo->prepare("UPDATE horaires SET places_disponibles = places_disponibles - 1 WHERE id = ?");
            $updateStmt->execute([$horaire_id]);
        } catch (Exception $e) {
            // Ignorer si la colonne n'existe pas
        }
        
        echo json_encode([
            "success" => true,
            "message" => "Commande enregistrée avec succès",
            "commande_id" => $commande_id
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Erreur lors de l'insertion", "debug" => $debug]);
    }
    
} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur BDD: " . $e->getMessage(),
        "debug" => $debug
    ]);
}
?>