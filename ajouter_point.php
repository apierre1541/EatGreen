<?php
require_once 'config.php';

header('Content-Type: application/json');

$input = json_decode(file_get_contents('php://input'), true);

function getValue($input, $keys, $default = '') {
    foreach ($keys as $key) {
        if (isset($input[$key]) && !empty(trim($input[$key]))) {
            return trim($input[$key]);
        }
    }
    return $default;
}
$nom_point = getValue($input, ['nom_point', 'nom']);
$description = getValue($input, ['description', 'desc']);
$latitude = getValue($input, ['latitude', 'lat']);
$longitude = getValue($input, ['longitude', 'lon', 'lng']);
$adresse = getValue($input, ['adresse', 'address', 'addr']);
$code_postal = getValue($input, ['code_postal', 'code postal', 'codePostal', 'cp', 'postal_code']);
$ville = getValue($input, ['ville', 'city', 'commune']);
$type_point = getValue($input, ['type_point', 'type point', 'type', 'category']);

$errors = [];

if (empty($nom_point)) {
    $errors[] = "Le nom du point est requis";
}

if (empty($latitude)) {
    $errors[] = "La latitude est requise";
} elseif (!is_numeric($latitude) || $latitude < -90 || $latitude > 90) {
    $errors[] = "La latitude doit être un nombre entre -90 et 90";
}

if (empty($longitude)) {
    $errors[] = "La longitude est requise";
} elseif (!is_numeric($longitude) || $longitude < -180 || $longitude > 180) {
    $errors[] = "La longitude doit être un nombre entre -180 et 180";
}

if (empty($adresse)) {
    $errors[] = "L'adresse est requise";
}

if (empty($code_postal)) {
    $errors[] = "Le code postal est requis";
}

if (empty($ville)) {
    $errors[] = "La ville est requise";
}

if (empty($type_point)) {
    $type_point = 'autre'; 
}

if (!empty($errors)) {
    echo json_encode([
        "success" => false,
        "message" => implode(", ", $errors)
    ]);
    exit;
}

try {
    $checkStmt = $pdo->prepare("SELECT id FROM points_carte WHERE nom_point = ? AND adresse = ?");
    $checkStmt->execute([$nom_point, $adresse]);
    
    if ($checkStmt->rowCount() > 0) {
        echo json_encode([
            "success" => false,
            "message" => "Ce point existe déjà"
        ]);
        exit;
    }
    
    $stmt = $pdo->prepare("INSERT INTO points_carte 
        (nom_point, description, latitude, longitude, adresse, code_postal, ville, type_point, date_creation) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())");
    
    $stmt->execute([
        $nom_point, 
        $description, 
        $latitude, 
        $longitude, 
        $adresse, 
        $code_postal, 
        $ville, 
        $type_point
    ]);
    
    echo json_encode([
        "success" => true,
        "message" => "Point ajouté avec succès",
        "id" => $pdo->lastInsertId(),
        "point" => [
            "id" => $pdo->lastInsertId(),
            "nom_point" => $nom_point,
            "latitude" => $latitude,
            "longitude" => $longitude,
            "adresse" => $adresse,
            "code_postal" => $code_postal,
            "ville" => $ville,
            "type_point" => $type_point
        ]
    ]);
    
} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Erreur BDD: " . $e->getMessage()
    ]);
}
?>