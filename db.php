<?php
// db.php - Connexion à la base de données
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

$host = "localhost";
$dbname = "eatgreen_db";
$username = "root";
$password = "";

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    $pdo->setAttribute(PDO::ATTR_DEFAULT_FETCH_MODE, PDO::FETCH_ASSOC);
    
    // Test de connexion (optionnel)
    error_log("Connexion à la base de données réussie");
    
} catch(PDOException $e) {
    // Journaliser l'erreur
    error_log("Erreur de connexion: " . $e->getMessage());
    
    // Renvoyer une réponse JSON d'erreur
    echo json_encode([
        "success" => false, 
        "message" => "Erreur de connexion à la base de données: " . $e->getMessage()
    ]);
    exit();
}
?>