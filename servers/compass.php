<?php


include 'pigeon-nelson.php';

$server = new PigeonNelsonServer($_GET);


$server->setName("Rose des vents");
$server->setDescription("Connaître la direction vers laquelle vous pointez");
$server->setEncoding("UTF-8");
$server->setDefaultPeriodBetweenUpdates(0);


if ($server->isRequestedSelfDescription()) {
    print $server->getSelfDescription();
    return;
}


// azimuth is required
if (!$server->hasAzimuthRequest()) {
    echo "[]";
    return;
}

$azimuthClock = $server->getAzimuthRequestAsClock();


$names = [ 0 => "Le Nord", 3 => "L'Est", 6 => "Le Sud", 9 => "L'Ouest", 12 => "Le Nord" ];

$message = "";

for($refHour = 0; $refHour <= 12; $refHour += 3) {
    $diff = PNUtil::clockDistance($azimuthClock, $refHour); 
    
    if ($diff <= 1.5) {
        if ($diff <= 0.5) {
            $message = $names[$refHour] . " est à midi.";
            break;
        }
        if ($azimuthClock - $refHour > 0) {
            $message = $names[$refHour] . " est à 11 heures.";
            break;
        }        
        else {
            $message = $names[$refHour] . " est à 1 heure.";
            break;
        }
    }
}

if ($message == "") {
    echo "[]";
    return;
}
else {
    $message = PigeonNelsonMessage::makeTxtMessage($message, "fr");
    $message->setPriority(0);
    print "[" . $message->toString() . "]";
    
}

?>

