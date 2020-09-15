<?php

/*
  Dependancies:

  * https://github.com/jsor/geokit (version 1.3.0)

 */
require __DIR__ . "/vendor/autoload.php";

use Geokit\LatLng;

$radiusPlayable = 500; /* meters */
$radiusSearch = 2; /* km */

$math = new Geokit\Math();

function osm2geokit($element) {
    if ($element["type"] == "node") {
        return new Geokit\LatLng($element["lat"], $element["lon"]);
    }
    else 
    if ($element["center"] != null) {
        return new Geokit\LatLng($element["center"]["lat"], $element["center"]["lon"]);
    }
}




function printMuseum($row) {
    global $radiusPlayable;

    $name = $row["tags"]["name"];
    $coordinate = osm2geokit($row);
    
    if (!isset($name)) {
        $name = "Vous êtes proche d'un musée dont on ne connaît pas le nom.";
    }
    else {
        $name = "Vous êtes proche d'un musée qui s'appelle " . $name;
    }

    print '{
        "txt": "' . $name . '",
        "lang": "fr",
        "priority": 1,
        "requiredConditions": [ {"reference": "distanceTo(' . $coordinate . ')", "comparison": "lessThan", "parameter": ' . $radiusPlayable . '} ],
        "forgettingConditions": [ ]}';
    
    return $coordinate;
}


$lat = $_GET["lat"];
$lng = $_GET["lng"];


// two parameters are required
if (!isset($lat) || !isset($lng)) {
    echo "[]";
    return;
}


// create a bounding box from the given position
$position =  new Geokit\LatLng($lat, $lng);
$box = $math->expand($position, $radiusSearch . 'km');
$box_str = "(" . $box->getSouthWest() . ",". $box->getNorthEast() . ")";


$overpass = 'http://overpass-api.de/api/interpreter?data=[out:json][timeout:25];(node[%22tourism%22=%22museum%22]'.$box_str.';way[%22tourism%22=%22museum%22]'.$box_str.';);out%20center;';

// collecting results in JSON format
$html = file_get_contents($overpass);
$result = json_decode($html, true); // "true" to get PHP array instead of an object


$data = $result['elements'];

print "[";

$minDist = new Geokit\Distance($radiusSearch, Geokit\Distance::UNIT_KILOMETERS);

if (count($data) != 0) {
    $first = true;
    foreach($data as $key => $row) {
        if (isset($row["tags"]) && isset($row["tags"]["tourism"]) && $row["tags"]["tourism"] == "museum") {
            if ($first)
                $first = false;
            else 
                echo ", ";
            $loc = printMuseum($row);
            $dist = $math->distanceHaversine($position, $loc);
            if ($dist < $minDist)
                $minDist = $dist;
        }
    }

}

if ($minDist->meters() > $radiusPlayable) {
    print ', {
        "txt": "Il n\'y a aucun musée autour de vous.",
        "lang": "fr",
        "priority": 0,
        "requiredConditions": [ ],
        "forgettingConditions": [ ]}';
}
    
print ']';

?>

