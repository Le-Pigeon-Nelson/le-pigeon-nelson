<?php

$seconds = date("s");

if ($seconds % 15 == 0) {
?>
[
    {
        "txt": "Ceci est un long message, diffusé toutes les 15 secondes, et dont le contenu se déroule pendant de nombreuses secondes. Il est interrompu dès que le serveur envoie un message avec une priorité plus grande, sous forme d'un bip très court.",
        "lang": "fr",
        "priority": 1,
        "requiredConditions": [],
        "forgettingConditions": []
}
]
<?php
}
else {

?>
[
    {
        "txt": "Bip!",
        "lang": "fr",
        "priority": 2,
        "requiredConditions": [],
        "forgettingConditions": [{ "reference": "timeFromReception", "comparison": "greaterThan", "parameter": 1}]
    }
]
<?php
}




?>
