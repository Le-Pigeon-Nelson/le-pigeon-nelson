<?php




include 'pigeon-nelson.php';

use Geokit\LatLng;


function get_redirect($url) {
    $headers = @get_headers($url);
    $final_url = "";
    foreach ($headers as $h)
    {
        if (substr($h,0,10) == 'Location: ')
        {
        $final_url = trim(substr($h,10));
        break;
        }
    }
    return $final_url;
}

$server = new PigeonNelsonServer($_GET);

$server->setName("Radio Garden");
$server->setDescription("Écouter une webradio à proximité");
$server->setEncoding("UTF-8");
$server->setDefaultPeriodBetweenUpdates(60 * 60); // reload every hour


if ($server->isRequestedSelfDescription()) {
    print $server->getSelfDescription();
    return;
}


// coordinates is required
if (!$server->hasCoordinatesRequest()) {
    $message = PigeonNelsonMessage::makeTxtMessage("Je n'ai pas réussi à vous localiser.", "fr");
    $message->setPriority(0);
    $server->addMessage($message);
}
else {    

        // find closest place
        $json = file_get_contents("http://radio.garden/api/ara/content/places");
        $obj = json_decode($json);
        $list = $obj->data->list;

        $position = $server->getPositionRequest();
        
        $distance = null;
        $selected = -1;
        foreach($list as $key => $value) {
            $loc = new Geokit\LatLng($value->geo[1], $value->geo[0]);
            $d = PNUtil::distance($loc, $position);
            if ($distance == null || $distance > $d) {
                $distance = $d;
                $selected = $key;
            }        
        }
        
        $place = $list[$selected]->title;
        
        
        // find radios
        $json = file_get_contents("https://radio.garden/api/ara/content/page/" . $list[$selected]->id);
        $obj = json_decode($json);
        $list = $obj->data->content[0]->items;
        
        $id = rand(0, count($list) - 2); // the last entry is a link to all stations
        
        $message = PigeonNelsonMessage::makeTxtMessage("Vous êtes proches de " . $place . ". Je vous propose d'écouter " . $list[$id]->title, "fr");
        $message->setPriority(2);
        $server->addMessage($message);
        
        
        // get official URL
        $url = explode("/", $list[$id]->href)[3];
        $url = "https://radio.garden/api/ara/content/listen/" . $url . "/channel.mp3";
        $url = get_redirect($url);
        
        $message = PigeonNelsonMessage::makeAudioMessage($url);
        $message->setPriority(1);
        $server->addMessage($message);

}
    
$server->printMessages();

?>

