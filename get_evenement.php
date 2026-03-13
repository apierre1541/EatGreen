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

// Requête SQL
$sql = "SELECT id, jour, mois, annee, titre, horaire FROM evenements ORDER BY annee, mois, jour";
$result = $conn->query($sql);

$evenements = [];

if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $evenements[] = [
            "id" => $row["id"],
            "jour" => $row["jour"],
            "mois" => $row["mois"],
            "annee" => $row["annee"],
            "titre" => $row["titre"],
            "horaire" => $row["horaire"]
        ];
    }
}

// Retour JSON
echo json_encode($evenements);
$conn->close();
?>