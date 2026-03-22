<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    $jour = isset($_GET['jour']) ? intval($_GET['jour']) : 0;
    $mois = isset($_GET['mois']) ? intval($_GET['mois']) : 0;
    $annee = isset($_GET['annee']) ? intval($_GET['annee']) : 0;

    // Si aucune date spécifique n'est fournie, on prend TOUS les cours
    if ($jour == 0 || $mois == 0 || $annee == 0) {
        // ✅ ORDRE CROISSANT (du plus ancien au plus récent)
        $stmt = $pdo->query("
            SELECT id, titre, horaire, jour, mois, annee 
            FROM evenements 
            ORDER BY annee ASC, mois ASC, jour ASC, horaire ASC
        ");
        $cours = $stmt->fetchAll(PDO::FETCH_ASSOC);
    } else {
        // ✅ ORDRE CROISSANT pour une date spécifique
        $stmt = $pdo->prepare("
            SELECT id, titre, horaire 
            FROM evenements 
            WHERE jour = ? AND mois = ? AND annee = ? 
            ORDER BY horaire ASC
        ");
        $stmt->execute([$jour, $mois, $annee]);
        $cours = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    $resultat = [];

    foreach ($cours as $c) {
        // Récupérer les inscrits pour ce cours
        $stmt2 = $pdo->prepare("
            SELECT u.nom, u.prenom, u.email
            FROM inscriptions_cours i
            JOIN users u ON i.user_id = u.id
            WHERE i.evenement_id = ?
            ORDER BY u.nom ASC, u.prenom ASC
        ");
        $stmt2->execute([$c['id']]);
        $inscrits = $stmt2->fetchAll(PDO::FETCH_ASSOC);

        $coursData = [
            "id" => $c['id'],
            "titre" => $c['titre'],
            "horaire" => $c['horaire']
        ];
        
        // Ajouter les infos de date si elles existent
        if (isset($c['jour'])) {
            $coursData['jour'] = $c['jour'];
            $coursData['mois'] = $c['mois'];
            $coursData['annee'] = $c['annee'];
        }
        
        $coursData['inscrits'] = $inscrits;
        
        $resultat[] = $coursData;
    }

    echo json_encode($resultat);

} catch(PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>