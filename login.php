<?php
// login.php - Connexion utilisateur
require_once 'config.php';

// Récupérer les données (POST ou JSON)
$email = "";
$password = "";

$contentType = isset($_SERVER['CONTENT_TYPE']) ? $_SERVER['CONTENT_TYPE'] : '';

if (strpos($contentType, 'application/json') !== false) {
    $jsonData = file_get_contents('php://input');
    $data = json_decode($jsonData, true);
    $email = isset($data['email']) ? trim($data['email']) : '';
    $password = isset($data['password']) ? $data['password'] : '';
} else {
    $email = isset($_POST['email']) ? trim($_POST['email']) : '';
    $password = isset($_POST['password']) ? $_POST['password'] : '';
}

if (empty($email) || empty($password)) {
    echo json_encode([
        "success" => false, 
        "message" => "Email et mot de passe requis"
    ]);
    exit();
}

try {
    // Récupérer l'utilisateur
    $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if (!$user) {
        echo json_encode([
            "success" => false, 
            "message" => "Email ou mot de passe incorrect"
        ]);
        exit();
    }

    // Vérifier le mot de passe
    if (!password_verify($password, $user['mot_de_passe'])) {
        echo json_encode([
            "success" => false, 
            "message" => "Email ou mot de passe incorrect"
        ]);
        exit();
    }


    // Récupérer les informations du restaurant si c'est un restaurateur
    $restaurant = null;
    if ($user['role'] === 'restaurateur') {
        $restoStmt = $pdo->prepare("SELECT * FROM restaurants WHERE user_id = ?");
        $restoStmt->execute([$user['id']]);
        $restaurant = $restoStmt->fetch();
    }

    // Mettre à jour la date de dernière connexion
    $updateStmt = $pdo->prepare("UPDATE users SET date_validation = NOW() WHERE id = ?");
    $updateStmt->execute([$user['id']]);

    // Préparer la réponse
    $response = [
        "success" => true,
        "message" => "Connexion réussie",
        "user" => [
            "id" => $user['id'],
            "nom" => $user['nom'],
            "prenom" => $user['prenom'],
            "email" => $user['email'],
            "telephone" => $user['telephone'],
            "role" => $user['role'],
            "date_inscription" => $user['date_inscription']
        ]
    ];

    // Ajouter les infos du restaurant si disponibles
    if ($restaurant) {
        $response['user']['restaurant'] = [
            "id" => $restaurant['id'],
            "nom_restaurant" => $restaurant['nom_restaurant'],
            "siret" => $restaurant['siret'],
            "adresse" => $restaurant['adresse'],
            "code_postal" => $restaurant['code_postal'],
            "commune" => $restaurant['commune']
        ];
    }

    echo json_encode($response);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur serveur: " . $e->getMessage()
    ]);
}
?>