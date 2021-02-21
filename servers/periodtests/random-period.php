[
    {
        "name": "Pause aléatoire",
        "description": "Une durée aléatoire entre deux messages",
        "encoding": "UTF-8",
        "defaultPeriod": 30
    
    },
<?php 
$period = rand(2, 10);

?>

    {
        "txt": "Prochain message dans <?php echo $period; ?> secondes.",
        "lang": "fr",
        "priority": 1,
        "period": <?php echo $period; ?>,
        "requiredConditions": [],
        "forgettingConditions": []
    }
]
