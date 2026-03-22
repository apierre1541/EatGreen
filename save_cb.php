<?php
include 'db.php';

// Configuration pour renvoyer du JSON propre
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Récupération et nettoyage des données
    $email = isset($_POST['email']) ? trim($_POST['email']) : '';
    $cb_numero = isset($_POST['cb_numero']) ? trim($_POST['cb_numero']) : '';
    $cb_date = isset($_POST['cb_date']) ? trim($_POST['cb_date']) : '';
    $cb_cvv = isset($_POST['cb_cvv']) ? trim($_POST['cb_cvv']) : '';
    
    // Vérification de l'email
    if (empty($email)) {
        echo json_encode(["status" => "error", "message" => "L'email utilisateur est manquant."]);
        exit;
    }

    try {
        // ATTENTION : Vérifie si ta table s'appelle 'utilisateurs' ou 'users'
        // Vérifie aussi si ta variable de connexion est $conn ou $pdo (voir db.php)
        $sql = "UPDATE users SET cb_numero = ?, cb_date = ?, cb_cvv = ? WHERE email = ?";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([$cb_numero, $cb_date, $cb_cvv, $email]);

        // rowCount() > 0 signifie qu'une ligne a été trouvée ET modifiée.
        // Si les données sont EXACTEMENT les mêmes que déjà en base, rowCount() peut renvoyer 0.
        if ($stmt->rowCount() > 0) {
            echo json_encode(["status" => "success", "message" => "Carte enregistrée !"]);
        } else {
            // On vérifie si l'utilisateur existe au moins
            $check = $pdo->prepare("SELECT id FROM users WHERE email = ?");
            $check->execute([$email]);
            if ($check->fetch()) {
                echo json_encode(["status" => "success", "message" => "Données identiques, aucune modification nécessaire."]);
            } else {
                echo json_encode(["status" => "error", "message" => "Aucun compte trouvé avec l'email : " . $email]);
            }
        }
    } catch (PDOException $e) {
        echo json_encode(["status" => "error", "message" => "Erreur SQL : " . $e->getMessage()]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Méthode non autorisée"]);
}
?>