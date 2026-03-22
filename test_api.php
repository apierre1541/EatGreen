<?php
// test_db.php - Test de connexion à la base de données
require_once 'db.php';

echo "<h2>Test de connexion à la base de données</h2>";

if ($pdo) {
    echo "<p style='color:green'>✅ Connexion établie avec succès!</p>";
    
    try {
        $stmt = $pdo->query("SELECT 1 as test");
        $result = $stmt->fetch();
        echo "<p style='color:green'>✅ Requête exécutée avec succès!</p>";
        
        // Afficher les tables disponibles
        $tables = $pdo->query("SHOW TABLES")->fetchAll();
        echo "<h3>Tables dans la base de données:</h3>";
        echo "<ul>";
        foreach ($tables as $table) {
            echo "<li>" . $table[0] . "</li>";
        }
        echo "</ul>";
        
    } catch (PDOException $e) {
        echo "<p style='color:red'>❌ Erreur de requête: " . $e->getMessage() . "</p>";
    }
} else {
    echo "<p style='color:red'>❌ La connexion a échoué (pdo est null)</p>";
}
?>