<?php
// create_restaurant.php - Création du restaurant pour un restaurateur
require_once 'config.php';

// Récupérer les données (POST ou JSON)
$data = $_POST;
if (empty($data)) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
}

// Validation des champs requis
$required = ['user_id', 'nom_restaurant', 'siret', 'adresse', 'code_postal', 'commune'];
foreach ($required as $field) {
    if (empty($data[$field])) {
        echo json_encode(["success" => false, "message" => "Champ $field requis"]);
        exit();
    }
}

$user_id = intval($data['user_id']);
$nom_restaurant = trim($data['nom_restaurant']);
$siret = trim($data['siret']);
$adresse = trim($data['adresse']);
$code_postal = trim($data['code_postal']);
$commune = trim($data['commune']);

// Validation du SIRET (14 chiffres)
if (!preg_match('/^\d{14}$/', $siret)) {
    echo json_encode(["success" => false, "message" => "Le SIRET doit contenir 14 chiffres"]);
    exit();
}

// Validation du code postal (5 chiffres)
if (!preg_match('/^\d{5}$/', $code_postal)) {
    echo json_encode(["success" => false, "message" => "Code postal invalide (5 chiffres)"]);
    exit();
}

try {
    // Vérifier que l'utilisateur existe et est bien un restaurateur
    $checkUser = $pdo->prepare("SELECT id, role FROM users WHERE id = ? AND role = 'restaurateur'");
    $checkUser->execute([$user_id]);
    
    if ($checkUser->rowCount() == 0) {
        echo json_encode(["success" => false, "message" => "Utilisateur non trouvé ou n'est pas un restaurateur"]);
        exit();
    }

    // Vérifier si le restaurant existe déjà pour cet utilisateur
    $checkResto = $pdo->prepare("SELECT id FROM restaurants WHERE user_id = ?");
    $checkResto->execute([$user_id]);
    
    if ($checkResto->rowCount() > 0) {
        echo json_encode(["success" => false, "message" => "Un restaurant est déjà associé à ce compte"]);
        exit();
    }

    // Vérifier si le SIRET existe déjà
    $checkSiret = $pdo->prepare("SELECT id FROM restaurants WHERE siret = ?");
    $checkSiret->execute([$siret]);
    
    if ($checkSiret->rowCount() > 0) {
        echo json_encode(["success" => false, "message" => "Ce numéro SIRET est déjà enregistré"]);
        exit();
    }

    // Insérer le restaurant
    $stmt = $pdo->prepare("INSERT INTO restaurants (user_id, nom_restaurant, siret, adresse, code_postal, commune) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->execute([$user_id, $nom_restaurant, $siret, $adresse, $code_postal, $commune]);

    echo json_encode([
        "success" => true,
        "message" => "Restaurant créé avec succès ! Vous pouvez maintenant vous connecter.",
        "restaurant_id" => $pdo->lastInsertId()
    ]);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de la création du restaurant: " . $e->getMessage()
    ]);
}
?>