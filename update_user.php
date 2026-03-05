<?php
include 'config.php';

if(isset($_POST['id'])){
    try {
        $sql = "UPDATE users SET nom = :nom, prenom = :prenom, email = :email WHERE id = :id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([
            'nom' => $_POST['nom'],
            'prenom' => $_POST['prenom'],
            'email' => $_POST['email'],
            'id' => $_POST['id']
        ]);
        
        echo "success";
    } catch (PDOException $e) {
        echo "error";
    }
}
?>