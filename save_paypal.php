<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = $_POST['email'];
    $paypal_email = isset($_POST['paypal_email']) ? $_POST['paypal_email'] : '';

    $sql = "UPDATE users SET paypal_email = ? WHERE email = ?";
    $stmt = $pdo->prepare($sql);
    
    if ($stmt->execute([$paypal_email, $email])) {
        echo json_encode(["status" => "success"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Erreur BDD"]);
    }
}
?>