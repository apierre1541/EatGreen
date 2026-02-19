<?php
// forgot_password.php - Demande de réinitialisation avec SMS
require_once 'config.php';

$data = $_POST;
if (empty($data)) {
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
}

$email = isset($data['email']) ? trim($data['email']) : '';

if (empty($email)) {
    echo json_encode(["success" => false, "message" => "Email requis"]);
    exit();
}

try {
    // Récupérer l'utilisateur pour obtenir son téléphone
    $userStmt = $pdo->prepare("SELECT id, telephone FROM users WHERE email = ?");
    $userStmt->execute([$email]);
    $user = $userStmt->fetch();
    
    if (!$user) {
        echo json_encode(["success" => false, "message" => "Email non trouvé"]);
        exit();
    }

    $telephone = $user['telephone'];
    if (empty($telephone)) {
        echo json_encode(["success" => false, "message" => "Aucun téléphone associé à ce compte"]);
        exit();
    }

    // Générer un token sécurisé
    $token = bin2hex(random_bytes(32));
    $expires = date('Y-m-d H:i:s', strtotime('+1 hour'));

    // Supprimer les anciens tokens pour cet email
    $deleteStmt = $pdo->prepare("DELETE FROM password_resets WHERE email = ?");
    $deleteStmt->execute([$email]);

    // Insérer le nouveau token
    $insertStmt = $pdo->prepare("INSERT INTO password_resets (email, token, expires_at) VALUES (?, ?, ?)");
    $insertStmt->execute([$email, $token, $expires]);

    // SIMULATION D'ENVOI DE SMS
    // Dans un projet réel, vous utiliseriez une API comme Twilio, TextBee, etc.
    $resetLink = "yourapp://reset-password?email=" . urlencode($email) . "&token=" . $token;
    
    // Message SMS simulé
    $smsMessage = "EatGreen: Lien de réinitialisation: " . $resetLink;
    
    // Appel à une API SMS (exemple avec TextBee)
    // sendSmsViaTextBee($telephone, $smsMessage);
    
    // Pour le développement, on simule l'envoi
    error_log("SMS envoyé à $telephone: $smsMessage");

    echo json_encode([
        "success" => true,
        "token" => $token,
        "message" => "Un SMS a été envoyé à votre téléphone avec le lien de réinitialisation"
    ]);

} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur: " . $e->getMessage()
    ]);
}

/**
 * Fonction pour envoyer un SMS via TextBee (exemple)
 * Nécessite un compte sur textbee.dev et l'installation de l'app Android
 */
function sendSmsViaTextBee($phoneNumber, $message) {
    $apiKey = 'VOTRE_API_KEY_TEXTBEE';
    $deviceId = 'VOTRE_DEVICE_ID';
    
    $ch = curl_init('https://api.textbee.dev/api/v1/gateway/devices/' . $deviceId . '/send-sms');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'x-api-key: ' . $apiKey
    ]);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([
        'recipients' => [$phoneNumber],
        'message' => $message
    ]));
    
    $response = curl_exec($ch);
    curl_close($ch);
    
    return json_decode($response, true);
}
?>