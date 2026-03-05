<?php
include 'config.php';

if(isset($_POST['id'])){
    $id = $_POST['id'];
    
    try {
        $sql = "DELETE FROM users WHERE id = :id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute(['id' => $id]);
        
        echo "success";
    } catch (PDOException $e) {
        echo "error";
    }
}
?>