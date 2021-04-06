<?php

function get_nom_num($nb, $feminine) {
    switch ($nb) {
        case 1: if ($feminine) return "première"; else return "premier";
        case 2: return "deuxième";
        case 3: return "troisième";
        case 4: return "quatrième";
        case 5: return "cinquième";
        case 6: return "sixième";
        case 7: return "septième";
        case 8: return "huitième";
        case 9: return "neuvième";
        case 10: return "dixième";
        case 11: return "onzième";
        case 12: return "douzième";
        case 13: return "treizième";
        case 14: return "quatorzième";
        case 15: return "quinzième";
        case 16: return "seixième";
        case 17: return "dix-septième";
        case 18: return "dix-huitième";
        case 19: return "dix-neuvième";
        case 20: return "vingtième";
        case 21: return "vingt-et-unième";
        default:
        case 22: return "vingt-deuxième";
    }
}
function get_liaison_nom_de_le($nom) {
    $first = $nom[0];
    // TODO consider gender and first letter, or use a lookup table as below
    return "du " . $nom;
}

include 'pigeon-nelson.php';


$server = new PigeonNelsonServer($_GET);

$server->setName("Le député du coin");
$server->setDescription("Qui est le député du coin, et que fait-il?");
$server->setEncoding("UTF-8");
$server->setDefaultPeriodBetweenUpdates(0);


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

        // find circonscription
        $position = $server->getPositionRequest();
        $url = "https://global.mapit.mysociety.org/point/4326/". $position["lng"] . "," . $position["lat"];
        $json = file_get_contents($url);
        $obj = json_decode($json);
        
        $found = false;
        foreach($obj as $key => $value) {
            if (strpos($value->type_name, "OSM Political Boundary (Circonscription Législative") === 0) {
                if (!property_exists($value->codes, "osm_attr_ref"))
                    continue;

                $ref = explode("-", $value->codes->osm_attr_ref);
                if (count($ref) != 2)
                    continue;
                $dep = $ref[0];
                $circ = intval($ref[1]);
                
                $found = true;
                break;
            }
        }
        
        
        
        if ($found) {
            $url = "https://www.nosdeputes.fr/deputes/enmandat/json";
            $json = file_get_contents($url);
            $obj = json_decode($json);
            $deputes = $obj->deputes;
            $selected = null;
            foreach($deputes as $key => $value) {
                $num_circo = intval($value->depute->num_circo);
                $num_deptmt = $value->depute->num_deptmt;
                if (strlen($num_deptmt) == 2)
                    $num_deptmt = "0" . $num_deptmt;
                if ((strpos($num_deptmt, $dep) === 0) && ($num_circo == $circ)) {
                    $selected = $value->depute;
                }
            }
            
            if ($selected != null) {
                // print_r($selected);
                $msg = "Vous êtes actuellement dans la " . get_nom_num($selected->num_circo, true) . " circonscription " . get_liaison_nom_de_le($selected->nom_circo) . ", et ";
                
                if ($selected->sexe[0] == "H")
                    $msg .= "le député local";
                else
                    $msg .= "la députée locale";
                $msg .= " s'appelle " . $selected->nom . ". ";
                
                if ($selected->sexe[0] == "H")
                    $msg .= "Il";
                else
                    $msg .= "Elle";
                $msg .= " appartient au " . $selected->groupe_sigle;
                if (count($selected->adresses) > 0) {
                    $msg .= ", et vous pourrez ";
                if ($selected->sexe[0] == "H")
                    $msg .= "le";
                else
                    $msg .= "la";
                    $msg .= " trouver à l'adresse " . $selected->adresses[0]->adresse . ".";
                }
                else 
                    $msg .= ", et n'a pas d'adresse connue.";
                    
                $msg .= " C'est actuellement son " . get_nom_num( $selected->nb_mandats, false) . " mandat.";
                
                $message = PigeonNelsonMessage::makeTxtMessage($msg, "fr");
                $message->setPriority(1);
                $server->addMessage($message);
            }
            else {
                $message = PigeonNelsonMessage::makeTxtMessage("Je n'ai pas réussi à identifier le député de la circonscription dans laquelle vous vous situez.", "fr");
                $message->setPriority(1);
                $server->addMessage($message);
            }
                
            
        }
        else {
            $message = PigeonNelsonMessage::makeTxtMessage("Je n'ai pas réussi à identifier la circonscription dans laquelle vous vous situez.", "fr");
            $message->setPriority(1);
            $server->addMessage($message);
        }

}
    
$server->printMessages();

?>

