<!doctype html>
<html lang="en">
  <head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-BmbxuPwQa2lc/FVzBcNJ7UAyJxM6wuqIj61tLrc4wSX0szH/Ev+nYRRuWlolflfl" crossorigin="anonymous">

    <!-- jquery -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    
    <!-- Leaflet -->
     <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" integrity="sha512-xodZBNTC5n17Xt2atTPuE1HxjVMSvLVW9ocqUKLsCC5CXdbqCmblAshOMAS6/keqq/sMZMZ19scR4PsZChSR7A==" crossorigin=""/>
    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js" integrity="sha512-XQoYMqMTK8LvdxXYG3nZ448hOEQiglfqkJs1NOQV44cWnUrBc8PkAOcXy20w0vlaXaVUearIOBhiXZ5V3ynxwA==" crossorigin=""></script>
    
    <title>Data browser for Le Pigeon Nelson</title>
  </head>
  <body>
  <div class="container">
   <h1><a href="<?php 
    $current_request = preg_replace("/\?.*$/","",$_SERVER["REQUEST_URI"]);
    echo $current_request; ?>">Data browser</a> for <strong>Le Pigeon Nelson</strong></h1>
<?php

$interval = 60;

include 'logger.php';

$logger = new Logger();


if (array_key_exists("rebuild", $_GET)) {
    $logger->rebuildSeriesDescriptions($interval);
}

$SUID = NULL;
if (array_key_exists("uid", $_GET))
    $SUID = $_GET["uid"];
if (strcmp($SUID, "all") == 0)
    $SUID = NULL;


$SSERIES = NULL;
if (array_key_exists("series", $_GET))
    $SSERIES = $_GET["series"];
if (strcmp($SSERIES, "all") == 0)
    $SSERIES = NULL;
    
if ($SSERIES != NULL && $SUID == NULL) {
    $elements = explode("-", $SSERIES);
    $elements=array_slice($elements,0,count($elements)-1);


    $SUID = implode($elements, "-");
    
}
    
    

$uids = $logger->getUIDs();

echo '<div class="container">';
echo '<div class="row">';
echo '<div class="col-sm">';
echo '<select class="form-select" aria-label="Device ID" id="devices">';
echo "<option ";
if ($SUID == NULL)
    echo "selected";
echo ' value="all">Device ID</option>';
    
foreach($uids as $uid) {
        echo "<option ";
        if (strcmp($SUID, $uid) == 0)
            echo "selected";
        echo ' value="'. $uid .'">'.$uid."</option>";
}
echo "</select>";

echo "</div>";
echo '<div class="col-sm">';

echo '<select class="form-select" aria-label="Series" id="series">';
$descriptions = $logger->getSeriesDescriptions($SUID, $interval);

echo "<option ";
if ($SSERIES == NULL)
    echo "selected";
echo ' value="all">Select a series</option>';

$SDEC = NULL;
foreach($descriptions as $desc) {
     $val = $desc->uid . "-" . $desc->start;
     echo "<option ";
        if (strcmp($SSERIES, $val) == 0) {
            echo "selected";
            $SDEC = $desc;
        }
        
        echo ' value="'. $val .'">'. $desc->toString() . "</option>";
        
}
if ($SSERIES == NULL) {
    $series = null;
}
else {
    $series = $logger->getSeries($SDEC);
}


echo "</select>";
echo "</div>";
echo '<div class="col-sm">';

echo '<button id="reset" class="btn btn-primary">Reset</button>';
echo "</div>";
echo "</div>";
echo "</div>";

?>
    <script>
        $(document).ready(function(){
            $('#series').change(function(){
                window.location.href = location.protocol + '//' + location.host + location.pathname + '?series=' + $(this).val();
            });
            $('#devices').change(function(){
                window.location.href = location.protocol + '//' + location.host + location.pathname + '?uid=' + $(this).val();
            });
            $('#reset').click(function(){
                window.location.href = location.protocol + '//' + location.host + location.pathname;
            });
            $('#rebuild').click(function(){
                window.location.href = location.protocol + '//' + location.host + location.pathname + "?rebuild";
            });
        });
    </script>

<div class="row top8" style="margin-top: 2em">
    <div class="col-sm">
<ul class="nav nav-tabs">
  <li class="nav-item">
    <a class="nav-link active" aria-current="page" id="map-tab" href="#map-container" data-bs-toggle="tab" data-bs-target="#map-container">Map</a>
  </li>
  <li class="nav-item">
    <a class="nav-link" id="table-tab" href="#table-container" data-bs-toggle="tab" data-bs-target="#table-container">Table</a>
  </li>
</ul>

<div class="tab-content">
  <div class="tab-pane active" id="map-container" role="tabpanel" aria-labelledby="map-tab">
            <div id="map" class="map map-home" style="height: 800px; margin-top: 20px"></div>
    </div>
    <div class="tab-pane" id="table-container" role="tabpanel" aria-labelledby="table-tab">
            <table class="table">
  <thead>
    <tr>
      <th scope="col">Server timestamp</th>
      <th scope="col">GPS timestamp</th>
      <th scope="col">latitude</th>
      <th scope="col">longitude</th>
      <th scope="col">accuracy (m)</th>
      <th scope="col">azimuth</th>
      <th scope="col">roll</th>
      <th scope="col">pitch</th>
    </tr>
  </thead>
  <tbody>
  <?php
  
    if ($series != NULL) {
        foreach($series->entries as $timestamp => $entry) {
            echo $entry->toHTMLArray();
        }
    
    }
    ?>
    
    </tbody>
    </table>
    </div>
</div>
</div>


<div class="container">
<div class="row">
<div class="col-sm">
<button id="rebuild" class="btn btn-primary">Rebuild series</button>
</div>
</div>
</div>

</div>

<script>
<?php
if ($series == NULL) {
    // default: Clermont-Ferrand
    echo "var map = L.map('map', {maxZoom: 21 }).setView([45.7871, 3.1127], 13);";
}
else {
    ?>
    var map = L.map('map').setView([45.7871, 3.1127], 13);
    
    var list = [];
    var centers = [];
    
    <?php
        foreach($series->entries as $timestamp => $entry) {
            echo "coords = L.latLng(" . $entry->getLat().", ". $entry->getLng() . ");\n";
            echo "centers.push(coords);\n";
            echo "c = L.circle(coords, {radius: ". $entry->getAccuracy() . "}).bindPopup('".$entry->toHTML()."');\n";
            echo "list.push(c);\n";
            $radius = 0.00001 * $entry->getAccuracy();
            $pitch = " * Math.abs(Math.sin(" . $entry->getPitch() . " * Math.PI / 180))";
            echo "var end_x = coords.lat + " . $radius . " * Math.cos(" . $entry->getAzimuth() . " * Math.PI / 180) " . $pitch . ";\n";
            echo "var end_y = coords.lng + " . $radius . " * Math.sin(" . $entry->getAzimuth() . " * Math.PI / 180) " . $pitch . ";\n";
            echo "var coordsshift = L.latLng(end_x, end_y);"; 
            echo 'var azimuth = L.polyline([coords, coordsshift], {color: "white"});';
            echo "list.push(azimuth);";
        }
    ?>
        
    var polyline = L.polyline(centers, {color: "#0000cc"}).addTo(map);
    var group = new L.featureGroup(list).addTo(map);
    
    map.fitBounds(group.getBounds());
    <?php
}
?>
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxNativeZoom:21,
        maxZoom:21
}).addTo(map);

</script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta2/dist/js/bootstrap.bundle.min.js" integrity="sha384-b5kHyXgcpbZJO/tY9Ul7kGkf1S0CWuKcCD38l8YkeH8z8QjE0GmW1gYU5S9FOnJ0" crossorigin="anonymous"></script>

    </div>
  </body>
</html>

