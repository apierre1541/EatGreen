<?php
include 'db.php'; // Assure-toi que $pdo est défini ici

try {
    // On vérifie que les données existent
    if (isset($_POST['id'], $_POST['nom_restaurant'], $_POST['siret'], $_POST['adresse'], $_POST['code_postal'], $_POST['commune'])) {
        
        $id = $_POST['id'];
        $nom = $_POST['nom_restaurant'];
        $siret = $_POST['siret'];
        $adr = $_POST['adresse'];
        $cp = $_POST['code_postal'];
        $com = $_POST['commune'];

        // Préparation de la requête avec PDO
        $sql = "UPDATE restaurants 
                SET nom_restaurant = :nom, 
                    siret = :siret, 
                    adresse = :adr, 
                    code_postal = :cp, 
                    commune = :com 
                WHERE id = :id";

        $stmt = $pdo->prepare($sql);

        // Liaison des paramètres
        $stmt->bindParam(':nom', $nom);
        $stmt->bindParam(':siret', $siret);
        $stmt->bindParam(':adr', $adr);
        $stmt->bindParam(':cp', $cp);
        $stmt->bindParam(':com', $com);
        $stmt->bindParam(':id', $id, PDO::PARAM_INT);

        if ($stmt->execute()) {
            // Avec PDO, on vérifie si une ligne a été modifiée
            if ($stmt->rowCount() > 0) {
                echo "success";
            } else {
                echo "Aucune modification (données identiques ou ID introuvable)";
            }
        } else {
            echo "Erreur lors de l'exécution";
        }
    } else {
        echo "Données POST manquantes";
    }
} catch (PDOException $e) {
    echo "Erreur BDD : " . $e->getMessage();
}
?>