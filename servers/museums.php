<?php



$radiusPlayable = 500; /* meters */
$radiusSearch = 2; /* km */


include 'pigeon-nelson.php';




function printMuseum($row) {
    global $radiusPlayable;

    $name = $row["tags"]["name"];
    $coordinate = PNUtil::osm2geokit($row);
    
    if (!isset($name)) {
        $name = "Vous êtes proche d'un musée dont on ne connaît pas le nom.";
    }
    else {
        $name = "Vous êtes proche d'un musée qui s'appelle " . $name;
    }

    $message = PigeonNelsonMessage::makeTxtMessage($name, "fr");
    $message->addRequiredCondition(PigeonNelsonCondition::ConditionDistanceTo($coordinate, Comparison::lessThan, $radiusPlayable));
    
    print $message->toString();
    
    return $coordinate;
}




$server = new PigeonNelsonServer($_GET);


// coordinates is required
if (!$server->hasRequestCoordinates()) {
    echo "[]";
    return;
}


$server->getOSMData('[out:json][timeout:25];(node["tourism"="museum"]({{box}});way["tourism"="museum"]({{box}}););out center;', $radiusSearch);

$position = $server->getRequestedPosition();



print "[";

$minDist = PNUtil::geoDistanceKm($radiusPlayable);

if ($server->hasEntries()) {
    $first = true;
    foreach($server->getEntries() as $key => $row) {
        if (isset($row["tags"]) && isset($row["tags"]["tourism"]) && $row["tags"]["tourism"] == "museum") {
            if ($first)
                $first = false;
            else 
                echo ", ";
            $loc = printMuseum($row);
            $dist = PNUtil::distance($position, $loc);
            if ($dist < $minDist)
                $minDist = $dist;
        }
    }

}

if ($minDist->meters() > $radiusPlayable) {
    $message = PigeonNelsonMessage::makeTxtMessage("Il n'y a aucun musée autour de vous.", "fr");
    $message->setPriority(0);
    print $message->toString();
}
    
print ']';

?>

