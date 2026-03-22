<?php
require_once 'config.php';

header('Content-Type: application/json');

$response = ["success" => false, "message" => ""];

try {
    $utilisateur_id = isset($_POST['users_id']) ? intval($_POST['users_id']) : 0;
    $plat_id = isset($_POST['plat_id']) ? intval($_POST['plat_id']) : 0;
    $quantite = isset($_POST['quantite']) ? intval($_POST['quantite']) : 1;

    if ($utilisateur_id == 0 || $plat_id == 0) {
        $response["message"] = "Paramètres manquants";
        echo json_encode($response);
        exit();
    }

    // ✅ Récupérer les infos du plat AVEC restaurant_id et nom_restaurant
    $stmt = $pdo->prepare("
        SELECT pr.prix, pr.quantite as quantite_disponible, pr.restaurant_id, r.nom_restaurant 
        FROM panier_repas pr
        LEFT JOIN restaurants r ON pr.restaurant_id = r.id
        WHERE pr.id = ?
    ");
    $stmt->execute([$plat_id]);
    $plat = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$plat) {
        $response["message"] = "Plat non trouvé";
        echo json_encode($response);
        exit();
    }

    $prix = $plat['prix'];
    $quantite_disponible = $plat['quantite_disponible'];
    $restaurant_id = $plat['restaurant_id'];
    $nom_restaurant = $plat['nom_restaurant'] ?? 'Restaurant inconnu';

    // Vérifier si l'utilisateur a déjà un panier actif
    $stmt = $pdo->prepare("SELECT id FROM paniers WHERE users_id = ? AND statut = 'actif'");
    $stmt->execute([$utilisateur_id]);
    $panier = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($panier) {
        $panier_id = $panier['id'];
        
        // ✅ Mettre à jour le restaurant_id du panier si nécessaire
        $stmt = $pdo->prepare("UPDATE paniers SET restaurant_id = ? WHERE id = ? AND restaurant_id IS NULL");
        $stmt->execute([$restaurant_id, $panier_id]);
        
    } else {
        // ✅ Créer un nouveau panier AVEC restaurant_id
        $stmt = $pdo->prepare("INSERT INTO paniers (users_id, restaurant_id) VALUES (?, ?)");
        $stmt->execute([$utilisateur_id, $restaurant_id]);
        $panier_id = $pdo->lastInsertId();
    }

    // Vérifier si le plat est déjà dans le panier
    $stmt = $pdo->prepare("SELECT id, quantite FROM panier_articles WHERE panier_id = ? AND plat_id = ?");
    $stmt->execute([$panier_id, $plat_id]);
    $article = $stmt->fetch(PDO::FETCH_ASSOC);

    // Calculer la quantité totale déjà commandée (tous utilisateurs)
    $stmt = $pdo->prepare("SELECT SUM(quantite) as total FROM panier_articles WHERE plat_id = ?");
    $stmt->execute([$plat_id]);
    $total_commandes = $stmt->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;
    
    $quantite_restante = $quantite_disponible - $total_commandes;

    if ($quantite_restante <= 0) {
        $response["message"] = "Plus de stock disponible";
        echo json_encode($response);
        exit();
    }

    if ($article) {
        // Vérifier que le total ne dépasse pas la quantité disponible
        if ($article['quantite'] + $quantite > $quantite_restante) {
            $response["message"] = "Quantité maximale atteinte";
            echo json_encode($response);
            exit();
        }
        
        // Mettre à jour la quantité
        $nouvelle_quantite = $article['quantite'] + $quantite;
        $stmt = $pdo->prepare("UPDATE panier_articles SET quantite = ? WHERE id = ?");
        $stmt->execute([$nouvelle_quantite, $article['id']]);
        $response["message"] = "Quantité mise à jour";
    } else {
        // ✅ Ajouter le plat au panier AVEC restaurant_id
        $stmt = $pdo->prepare("INSERT INTO panier_articles (panier_id, plat_id, restaurant_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?, ?)");
        $stmt->execute([$panier_id, $plat_id, $restaurant_id, $quantite, $prix]);
        $response["message"] = "Plat ajouté au panier";
    }

    // Récupérer le nombre total d'articles dans le panier pour ce plat
    $stmt = $pdo->prepare("SELECT SUM(quantite) as total FROM panier_articles WHERE panier_id = ? AND plat_id = ?");
    $stmt->execute([$panier_id, $plat_id]);
    $total_articles = $stmt->fetch(PDO::FETCH_ASSOC)['total'] ?? 0;

    // ✅ Ajouter les infos du restaurant à la réponse
    $response["success"] = true;
    $response["total_articles"] = $total_articles;
    $response["quantite_restante"] = $quantite_restante - $quantite;
    $response["restaurant_id"] = $restaurant_id;
    $response["nom_restaurant"] = $nom_restaurant;

} catch(PDOException $e) {
    $response["message"] = "Erreur BDD: " . $e->getMessage();
}

echo json_encode($response);
?>