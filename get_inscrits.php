<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    $jour = isset($_GET['jour']) ? intval($_GET['jour']) : 0;
    $mois = isset($_GET['mois']) ? intval($_GET['mois']) : 0;
    $annee = isset($_GET['annee']) ? intval($_GET['annee']) : 0;

    if ($jour == 0 || $mois == 0 || $annee == 0) {
        echo json_encode([]);
        exit();
    }

    $stmt = $pdo->prepare("
        SELECT 
            u.nom, 
            u.prenom, 
            u.email,
            e.horaire as heure,
            e.titre
        FROM inscriptions_cours i
        JOIN users u ON i.user_id = u.id
        JOIN evenements e ON i.evenement_id = e.id
        WHERE i.jour = ? AND i.mois = ? AND i.annee = ?
        ORDER BY e.horaire
    ");
    $stmt->execute([$jour, $mois, $annee]);
    $inscrits = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode($inscrits);

} catch(PDOException $e) {
    echo json_encode([]);
}
?>