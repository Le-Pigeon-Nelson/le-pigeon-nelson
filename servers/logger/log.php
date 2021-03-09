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
        $message = PigeonNelsonMessage::makeTxtMessage("donnée enregistrée", "fr");
    }
    else {
        $message = PigeonNelsonMessage::makeTxtMessage("erreur pendant l'enregistrmeent", "fr");
    }
}

$message->setPriority(0);
$server->addMessage($message);    
$server->printMessages();

?>

 
