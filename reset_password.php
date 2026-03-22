<?php
// reset_password.php - Réinitialisation du mot de passe
require_once 'config.php';

$data = $_POST;
if (empty($data)) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
}

$email = isset($data['email']) ? trim($data['email']) : '';
$token = isset($data['token']) ? trim($data['token']) : '';
$new_password = isset($data['new_password']) ? $data['new_password'] : '';

if (empty($email) || empty($token) || empty($new_password)) {
    echo json_encode(["success" => false, "message" => "Tous les champs sont requis"]);
    exit();
}

// Validation CNIL du nouveau mot de passe
if (strlen($new_password) < 12) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins 12 caractères"]);
    exit();
}

if (!preg_match('/[A-Z]/', $new_password)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins une majuscule"]);
    exit();
}

if (!preg_match('/[a-z]/', $new_password)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins une minuscule"]);
    exit();
}

if (!preg_match('/[0-9]/', $new_password)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins un chiffre"]);
    exit();
}

if (!preg_match('/[@#$%^&+=!]/', $new_password)) {
    echo json_encode(["success" => false, "message" => "Le mot de passe doit contenir au moins un caractère spécial (@#$%^&+=!)"]);
    exit();
}

try {
    // Vérifier le token
    $stmt = $pdo->prepare("SELECT * FROM password_resets WHERE email = ? AND token = ? AND used = 0 AND expires_at > NOW()");
    $stmt->execute([$email, $token]);
    $reset = $stmt->fetch();

    if (!$reset) {
        echo json_encode(["success" => false, "message" => "Lien invalide ou expiré"]);
        exit();
    }

    // Hacher le nouveau mot de passe
    $hashedPassword = password_hash($new_password, PASSWORD_DEFAULT);

    // Mettre à jour le mot de passe
    $updateStmt = $pdo->prepare("UPDATE users SET mot_de_passe = ? WHERE email = ?");
    $updateStmt->execute([$hashedPassword, $email]);

    // Marquer le token comme utilisé
    $markStmt = $pdo->prepare("UPDATE password_resets SET used = 1 WHERE id = ?");
    $markStmt->execute([$reset['id']]);

    echo json_encode([
        "success" => true,
        "message" => "Mot de passe réinitialisé avec succès"
    ]);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur: " . $e->getMessage()
    ]);
}
?>