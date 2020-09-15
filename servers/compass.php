<?php


include 'pigeon-nelson.php';

$server = new PigeonNelsonServer($_GET);


// azimuth is required
if (!$server->hasRequestedAzimuth()) {
    echo "[]";
    return;
}

$azimuthClock = $server->getRequestedAzimuthAsClock();


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

