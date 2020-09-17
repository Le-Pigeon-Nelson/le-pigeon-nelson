<?php



$radiusPlayable = 500; /* meters */
$radiusSearch = 2000; /* meters */


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

$server->setName("Musées");
$server->setDescription("Connaître les musées dans son voisinage");
$server->setEncoding("UTF-8");
$server->setDefaultPeriodBetweenUpdates(0);


if ($server->isRequestedSelfDescription()) {
    print $server->getSelfDescription();
    return;
}


// coordinates is required
if (!$server->hasCoordinatesRequest()) {
    echo "[]";
    return;
}


$server->getOSMData('[out:json][timeout:25];(node["tourism"="museum"]({{box}});way["tourism"="museum"]({{box}}););out center;', $radiusSearch);

$position = $server->getPositionRequest();



print "[";

$minDist = PNUtil::geoDistanceMeters($radiusPlayable);

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

