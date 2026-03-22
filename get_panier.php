<?php
require_once 'config.php';

header('Content-Type: application/json');

$response = ["success" => false, "message" => "", "articles" => [], "total" => 0];  // ← Ajout de "total"

try {
    $utilisateur_id = isset($_GET['users_id']) ? intval($_GET['users_id']) : 0;

    if ($utilisateur_id == 0) {
        $response["message"] = "Utilisateur non spécifié";
        echo json_encode($response);
        exit();
    }

    // 🔍 Récupérer le panier actif
    $stmt = $pdo->prepare("SELECT id FROM paniers WHERE users_id = ? AND statut = 'actif'");
    $stmt->execute([$utilisateur_id]);
    $panier = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$panier) {
        $response["success"] = true;
        $response["articles"] = [];
        $response["total"] = 0;  // ← Total à 0
        $response["message"] = "Panier vide";
        echo json_encode($response);
        exit();
    }

    $panier_id = $panier['id'];

    // ✅ REQUÊTE AVEC GESTION DES NULL
    $stmt = $pdo->prepare("
        SELECT 
            pa.id as article_id,
            pa.quantite,
            pa.prix_unitaire,
            pr.id as plat_id,
            pr.nom_plat,
            COALESCE(pr.restaurant_id, 0) as restaurant_id,
            pr.quantite,
            COALESCE(r.nom_restaurant, 'Restaurant inconnu') as nom_restaurant,
            (pr.quantite - IFNULL((SELECT SUM(quantite) FROM panier_articles WHERE plat_id = pr.id), 0)) as quantite_restante,
            (pa.quantite * pa.prix_unitaire) as total_ligne
        FROM panier_articles pa
        JOIN panier_repas pr ON pa.plat_id = pr.id
        LEFT JOIN restaurants r ON pr.restaurant_id = r.id
        WHERE pa.panier_id = ?
    ");
    $stmt->execute([$panier_id]);
    $articles = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Calculer le total général
    $totalGeneral = 0;
    
    if (!empty($articles)) {
        // Calculer le total à partir des articles
        foreach ($articles as $article) {
            $totalGeneral += $article['total_ligne'];
        }
        
        // ✅ S'assurer que restaurant_id n'est pas NULL
        $response["restaurant_id"] = $articles[0]['restaurant_id'] ?? 0;
        $response["nom_restaurant"] = $articles[0]['nom_restaurant'] ?? 'Restaurant inconnu';
    } else {
        // ✅ Valeurs par défaut si panier vide
        $response["restaurant_id"] = 0;
        $response["nom_restaurant"] = 'Aucun restaurant';
    }

    $response["success"] = true;
    $response["articles"] = $articles;
    $response["total"] = $totalGeneral;  // ← AJOUT DU TOTAL
    $response["message"] = "Panier chargé avec succès";

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>