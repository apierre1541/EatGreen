<?php
require_once 'config.php';

header('Content-Type: application/json');

try {
    // ✅ REQUÊTE AVEC JOINTURE POUR LE NOM DU RESTAURANT
    $stmt = $pdo->query("
        SELECT 
            pr.id, 
            pr.nom_plat, 
            pr.photo, 
            pr.condition_repas, 
            pr.prix, 
            pr.quantite, 
            pr.restaurant_id,
            r.nom_restaurant,
            pr.adresse_postal, 
            pr.code_postal, 
            pr.commune 
        FROM panier_repas pr
        LEFT JOIN restaurants r ON pr.restaurant_id = r.id
        ORDER BY pr.id DESC
    ");
    $plats = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode($plats);
    
} catch(PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>