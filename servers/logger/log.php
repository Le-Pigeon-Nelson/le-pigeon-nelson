<?php


include 'pigeon-nelson.php';
include 'logs/logger.php';

$server = new PigeonNelsonServer($_GET);


$server->setName("Logger");
$server->setDescription("Un serveur qui enregistre les requêtes envoyées toutes les 5 secondes, utilisé pour le debug.");
$server->setEncoding("UTF-8");
$server->setDefaultPeriodBetweenUpdates(5);


if ($server->isRequestedSelfDescription()) {
    print $server->getSelfDescription();
    return;
}


// coordinates is required
if (!$server->hasCoordinatesRequest()) {
    $message = PigeonNelsonMessage::makeTxtMessage("coordonnées manquantes", "fr");
}
else {
    
    $logger = new Logger();
    if ($logger->log($server->getParameters())) {
        if (array_key_exists("loc_timestamp", $server->getParameters())) {
        $shift = intval(time() - $server->getParameters()["loc_timestamp"] / 1000);
        if ($shift < 60) {
            $shiftHuman = intval($shift) . " secondes";
        }
        else {
            $shift %= 60;
            if ($shift < 60) {
                $shiftHuman = intval($shift) . " minutes";
            }
            else {
                $shift %= 60;
                $shiftHuman = intval($shift) . " heures";
            }
        }
        
        if ($shift > 0)
            $message = PigeonNelsonMessage::makeTxtMessage("Acquisition il y a " . $shiftHuman . ", précision de " . intval($server->getParameters()["loc_accuracy"]) . " mètres", "fr");
        else
            $message = PigeonNelsonMessage::makeTxtMessage("donnée enregistrée (timestamp décalé), précision de " . intval($server->getParameters()["loc_accuracy"]) . " mètres", "fr");
        }
        else
            $message = PigeonNelsonMessage::makeTxtMessage("donnée enregistrée (sans timestamp), précision de " . intval($server->getParameters()["loc_accuracy"]) . " mètres", "fr");
    }
    else {
        $message = PigeonNelsonMessage::makeTxtMessage("erreur pendant l'enregistrmeent", "fr");
    }
}

$message->setPriority(0);
$server->addMessage($message);    
$server->printMessages();

?>

 
