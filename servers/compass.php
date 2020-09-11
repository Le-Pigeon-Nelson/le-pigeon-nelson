<?php


function degreeToClock($azimuth) {
    return $azimuth / 360 * 12;
}

function fmod_alt($x, $y) {
    if (!$y) { return NAN; }
    $r = fmod($x, $y);
    if ($r < 0)
        return $r += $y;
    else
        return $r;
}

$azimuth = $_GET["azimuth"];


// one parameters is required
if (!isset($azimuth)) {
    echo "[]";
    return;
}

$azimuthClock = degreeToClock($azimuth);


$names = [ 0 => "Le Nord", 3 => "L'Est", 6 => "Le Sud", 9 => "L'Ouest", 12 => "Le Nord" ];

$message = "";

for($refHour = 0; $refHour <= 12; $refHour += 3) {
    $diff = fmod_alt(abs($azimuthClock - $refHour), 12);
    
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
    echo '[{ 
        "txt": "'. $message . '",
        "lang": "fr",
        "priority": 0,
        "requiredConditions": [ ],
        "forgettingConditions": [ ]
    }]';
    
}

?>

