<?php

header("Content-Type: application/json; charset=UTF-8");

$host = "localhost";
$dbname = "eatgreen_db";
$username = "root";
$password = "";

// Connexion à la base
$conn = new mysqli($host, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode([
        "success" => false,
        "message" => "Connexion échouée"
    ]);
    exit();
}

// Vérifier si les champs existent
if (
    isset($_POST['jour']) &&
    isset($_POST['mois']) &&
    isset($_POST['annee']) &&
    isset($_POST['titre']) &&
    isset($_POST['horaire'])
) {
    $jour = intval($_POST['jour']);
    $mois = intval($_POST['mois']);
    $annee = intval($_POST['annee']);
    $titre = trim($_POST['titre']);
    $horaire = trim($_POST['horaire']);

    // Préparation de la requête
    $stmt = $conn->prepare("INSERT INTO evenements (jour, mois, annee, titre, horaire) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("iiiss", $jour, $mois, $annee, $titre, $horaire);

    if ($stmt->execute()) {
        echo json_encode([
            "success" => true,
            "message" => "Événement ajouté"
        ]);
    }

    else {
        echo json_encode([
            "success" => false,
            "message" => "Erreur lors de l'ajout"
        ]);
    }
    $stmt->close();

}

else {
    echo json_encode([
        "success" => false,
        "message" => "Paramètres manquants"
    ]);
}
$conn->close();
?>