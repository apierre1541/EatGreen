<?php
require_once 'config.php';

header('Content-Type: application/json');

$jour = isset($_GET['jour']) ? intval($_GET['jour']) : 0;
$mois = isset($_GET['mois']) ? intval($_GET['mois']) : 0;
$annee = isset($_GET['annee']) ? intval($_GET['annee']) : 0;

if ($jour == 0 || $mois == 0 || $annee == 0) {
    echo json_encode(["error" => "Paramètres manquants"]);
    exit();
}

try {
    $stmt = $pdo->prepare("SELECT id, titre, horaire FROM evenements WHERE jour = ? AND mois = ? AND annee = ? ORDER BY horaire");
    $stmt->execute([$jour, $mois, $annee]);
    $evenements = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode($evenements);
    
} catch(PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>