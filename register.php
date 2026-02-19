<?php
// register.php - Inscription utilisateur avec validation CNIL
require_once 'config.php';

// Récupérer les données (POST ou JSON)
$data = $_POST;
if (empty($data)) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
}

// Validation des champs requis
$required = ['nom', 'prenom', 'email', 'telephone', 'role', 'mot_de_passe', 'confirm_mot_de_passe'];
foreach ($required as $field) {
    if (empty($data[$field])) {
        echo json_encode(["success" => false, "message" => "Champ $field requis"]);
        exit();
    }
}

$nom = trim($data['nom']);
$prenom = trim($data['prenom']);
$email = trim($data['email']);
$telephone = trim($data['telephone']);
$role = trim($data['role']);
$mot_de_passe = $data['mot_de_passe'];
$confirm_mot_de_passe = $data['confirm_mot_de_passe'];

// Validation du mot de passe (recommandations CNIL)
if ($mot_de_passe !== $confirm_mot_de_passe) {
    echo json_encode(["success" => false, "message" => "Les mots de passe ne correspondent pas"]);
    exit();
}

// Validation CNIL : 12 caractères minimum
if (strlen($mot_de_passe) < 12) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins 12 caractères"]);
    exit();
}

// Au moins une majuscule
if (!preg_match('/[A-Z]/', $mot_de_passe)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins une majuscule"]);
    exit();
}

// Au moins une minuscule
if (!preg_match('/[a-z]/', $mot_de_passe)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins une minuscule"]);
    exit();
}

// Au moins un chiffre
if (!preg_match('/[0-9]/', $mot_de_passe)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins un chiffre"]);
    exit();
}

// Au moins un caractère spécial
if (!preg_match('/[@#$%^&+=!]/', $mot_de_passe)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins un caractère spécial (@#$%^&+=!)"]);
    exit();
}

// Validation de l'email
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    echo json_encode(["success" => false, "message" => "Format d'email invalide"]);
    exit();
}

// Validation du rôle
$roles_valides = ['etudiant', 'admin', 'restaurateur'];
if (!in_array($role, $roles_valides)) {
    echo json_encode(["success" => false, "message" => "Rôle invalide"]);
    exit();
}

// Validation spécifique pour les emails UNILIM
if ($role === 'etudiant' && !preg_match('/@etu\.unilim\.fr$/', $email)) {
    echo json_encode(["success" => false, "message" => "Les étudiants doivent utiliser un email @etu.unilim.fr"]);
    exit();
}

if ($role === 'admin' && !preg_match('/@unilim\.fr$/', $email)) {
    echo json_encode(["success" => false, "message" => "Les administrateurs doivent utiliser un email @unilim.fr"]);
    exit();
}

// Validation du téléphone
if (!preg_match('/^[0-9]{10}$/', $telephone)) {
    echo json_encode(["success" => false, "message" => "Numéro de téléphone invalide (10 chiffres)"]);
    exit();
}

try {
    // Vérifier si l'email existe déjà
    $checkStmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
    $checkStmt->execute([$email]);
    
    if ($checkStmt->rowCount() > 0) {
        echo json_encode(["success" => false, "message" => "Cet email est déjà utilisé"]);
        exit();
    }

    // Hacher le mot de passe
    $hashedPassword = password_hash($mot_de_passe, PASSWORD_DEFAULT);

    // Insérer l'utilisateur
    $stmt = $pdo->prepare("INSERT INTO users (nom, prenom, telephone, email, role, mot_de_passe) VALUES (?, ?, ?, ?, ?, ?)");
    $stmt->execute([$nom, $prenom, $telephone, $email, $role, $hashedPassword]);
    
    $userId = $pdo->lastInsertId();

    $response = [
        "success" => true,
        "message" => "Inscription réussie",
        "user_id" => $userId,
        "role" => $role
    ];

    // Si c'est un restaurateur, on indique qu'il doit maintenant créer son restaurant
    if ($role === 'restaurateur') {
        $response['next_step'] = 'create_restaurant';
        $response['message'] = "Inscription réussie ! Veuillez maintenant créer votre restaurant.";
    }

    echo json_encode($response);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur lors de l'inscription: " . $e->getMessage()
    ]);
}
?>