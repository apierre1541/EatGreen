<?php
header('Content-Type: application/json');
include 'config.php';

try {
    // On vérifie si $pdo existe bien
    if (!isset($pdo)) {
        echo json_encode(["error" => "La variable pdo n'est pas definie dans db_config.php"]);
        exit;
    }

    $sql = "SELECT id, nom, prenom, email, role FROM users WHERE role != 'admin'";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    
    $users = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Si la liste est vide, on renvoie un tableau vide [] au lieu de rien du tout
    echo json_encode($users);

} catch (Exception $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>