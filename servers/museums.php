<?php



$radiusPlayable = 500; /* meters */
$radiusSearch = 2000; /* meters */


include 'pigeon-nelson.php';




function addMuseum($row, $server) {
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
    
    $server->addMessage($message);
    
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




$minDist = PNUtil::geoDistanceMeters($radiusPlayable);

if ($server->hasEntries()) {
    foreach($server->getEntries() as $key => $row) {
        if (isset($row["tags"]) && isset($row["tags"]["tourism"]) && $row["tags"]["tourism"] == "museum") {
            $loc = addMuseum($row, $server);
            $dist = PNUtil::distance($position, $loc);
            if ($dist < $minDist)
                $minDist = $dist;
        }
    }

}

if ($minDist->meters() >= $radiusPlayable) {
    $message = PigeonNelsonMessage::makeTxtMessage("Il n'y a aucun musée autour de vous.", "fr");
    $message->setPriority(0);
    $server->addMessage($message);
}
    
$server->printMessages();

?>

