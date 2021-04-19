<?php 

/*
  Dependancies:

  * https://github.com/jsor/geokit (version 1.3.0)

 */
require __DIR__ . "/vendor/autoload.php";

use Geokit\LatLng;



abstract class PNUtil {
    public static $math;
    
    public static function osm2geokit($element) {
        if ($element["type"] == "node") {
            return new Geokit\LatLng($element["lat"], $element["lon"]);
        }
        else 
        if ($element["center"] != null) {
            return new Geokit\LatLng($element["center"]["lat"], $element["center"]["lon"]);
        }
    }


    public static function degreeToClock($angle) {
        return $angle / 360 * 12;
    }

    private static function fmod_alt($x, $y) {
        if (!$y) { return NAN; }
        $r = fmod($x, $y);
        if ($r < 0)
            return $r += $y;
        else
            return $r;
    }
    
    public static function clockDistance($h1, $h2) {
        return PNUtil::fmod_alt(abs($h1 - $h2), 12);
    }
    
    public static function geoDistanceMeters($distance) {
        return new Geokit\Distance($distance, Geokit\Distance::UNIT_METERS);
    }
    
    public static function distance($position1, $position2) {
        return PNUtil::$math->distanceHaversine($position1, $position2);
    }

};

PNUtil::$math = new Geokit\Math();


abstract class Comparison
{
    const lessThan = 0;
    const lessOrEqualTo = 1;
    const greaterThan = 2;
    const greaterOrEqualTo = 3;

    public static function toString($comparison) {
        switch($comparison) {
            case Comparison::lessThan:
                return "<";
                break;
            case Comparison::lessOrEqualTo:
                return "<=";
                break;
            case Comparison::greaterThan:
                return ">";
                break;
            case Comparison::greaterOrEqualTo:
                return ">=";
                break;
            default:
                return "";
        }
                
    }
}

class PigeonNelsonCondition {

    public function __construct($reference, $comparison, $parameter) {
        $this->reference = $reference;
        $this->comparison = $comparison;
        $this->parameter = $parameter;
    }
    
    public static function ConditionDistanceTo($coordinates, $comparison, $parameter) {
        return new PigeonNelsonCondition("distanceTo(" . $coordinates. ")", $comparison, $parameter);
    }
    
    public static function ConditionTimeFromReception($comparison, $parameter) {
        return new PigeonNelsonCondition("timeFromReception", $comparison, $parameter);
    }
    
    
    public function toString() {
        return '{"reference": "' . $this->reference . '", "comparison": "'. Comparison::toString($this->comparison) . '", "parameter": "' . $this->parameter .'" }';
    }

};


class PigeonNelsonMessage {
    private $txt;
    private $lang;
    private $audioURL;
    private $priority;
    private $requiredConditions;
    private $forgettingConditions;
    
    public function __construct() {
        $this->txt = null;
        $this->lang = null;
        $this->audioURL = null;
        $this->priority = 1;
        $this->period = null;
        $this->requiredConditions = [];
        $this->forgettingConditions = [];
    }
    
    public static function makeTxtMessage($txt, $lang) {
        $result = new PigeonNelsonMessage();
        $result->txt = $txt;
        $result->lang = $lang;
        return $result;
    }
    public static function makeAudioMessage($audioURL) {
        $result = new PigeonNelsonMessage();
        $result->audioURL = $audioURL;
        return $result;
    }
    public function setPriority($priority) {
        $this->priority = $priority;
    }
    public function setPeriod($period) {
        $this->period = $period;
    }
    public function addRequiredCondition($condition) {
        array_push($this->requiredConditions, $condition);
    }
    public function addForgettingCondition($condition) {
        array_push($this->forgettingConditions, $condition);
    }
    public function addForgettingConditionTimeFromReception($duration) {
        $this->addForgettingCondition(PigeonNelsonCondition::ConditionTimeFromReception(Comparison::greaterThan, $duration));
    }
    
    public function hasForgettingConditionTFR() {
        foreach($this->forgettingConditions as $condition) {
            if ($condition->reference == "timeFromReception")
                return true;
        }
        return false;
    }
    
    public function toString() {
        $result = '{';
        if ($this->txt != null) {
            $result .= '"txt": "' . $this->txt . '",';
        }
        if ($this->lang != null) {
            $result .= '"lang": "' . $this->lang . '",';
        }
        if ($this->audioURL != null) {
            $result .= '"audioURL": "' . $this->audioURL . '",';
        }
        if ($this->priority != null) {
            $result .= '"priority": '. $this->priority .',';
        }
        if ($this->period != null) {
            $result .= '"period": '. $this->period .',';
        }
        $result .= '"requiredConditions": [';
        $first = true;
        foreach($this->requiredConditions as $condition) {
            if ($first) $first = false;
            else $result .= ", ";
            $result .= $condition->toString();
        }
        $result .= '], ';
        $result .= '"forgettingConditions": [';
        $first = true;
        foreach($this->forgettingConditions as $condition) {
            if ($first) $first = false;
            else $result .= ", ";
            $result .= $condition->toString();
        }
        $result .= ']}';
        
        return $result;
    }
        
}

class PigeonNelsonServer {
    private $getParams;
    private $requestedSelfDescription;
    private $name;
    private $description;
    private $encoding;
    private $defaultPeriodBetweenUpdates;
    private $messages;
    
    public function __construct($get) {
        $this->messages = [];
        $this->requestedSelfDescription = array_key_exists("self-description", $get);
        $this->getParams = array();
        
        $entries = [ "lat", "lng", "loc_accuracy", "azimuth", "pitch", "roll", "uid", "loc_timestamp" ];
        foreach($entries as $entry) {
            if (array_key_exists($entry, $get))
            $this->getParams[$entry] = $get[$entry];
        }

        $this->data = [];
        
        $name = null;
        $description = null;
        $encoding = null;
        $defaultPeriodBetweenUpdates = null;
    }
    
    
    public function setName($name) {
        $this->name = $name;
    }
    public function setDescription($description) {
        $this->description = $description;
    }
    public function setEncoding($encoding) {
        $this->encoding = $encoding;
    }
    public function setDefaultPeriodBetweenUpdates($defaultPeriodBetweenUpdates) {
        $this->defaultPeriodBetweenUpdates = $defaultPeriodBetweenUpdates;
    }

    public function getParameters() {
        return $this->getParams;
    }
        
    public function isRequestedSelfDescription() {
        return $this->requestedSelfDescription;
    }
    
    public function getSelfDescription() {
        return '[{ "name": "' . $this->name . '", "description": "'. $this->description . '", "encoding": "'. $this->encoding .'", "defaultPeriod": '. $this->defaultPeriodBetweenUpdates . ' }]';
    }
    

    public function hasAzimuthRequest() {
        return array_key_exists("azimuth", $this->getParams);
    }

    public function hasPitchRequest() {
        return array_key_exists("pitch", $this->getParams);
    }

    public function hasRollRequest() {
        return array_key_exists("roll", $this->getParams);        
    }

    public function hasCoordinatesRequest() {
    return array_key_exists("lng", $this->getParams) && array_key_exists("lat", $this->getParams);
    }
    
    public function hasCoordinatesAccuracy() {
        return array_key_exists("loc_accuracy", $this->getParams);
    }
    
    public function getCoordinatesAccuracy() {
        return $this->getParams["loc_accuracy"];
    }

    public function hasUid() {
        return array_key_exists("uid", $this->getParams);
    }
    public function getUid() {
        return $this->getParams["uid"];
    }
    
    private static function replaceBoxInRequest($request, $box_str) {
        $result = str_replace("{{box}}", $box_str, $request);
        $result = str_replace(" ", "%20", $result);
        $result = str_replace("\"", "%22", $result);
        return $result;
    }
    
    public function getPositionRequest() {
        return new Geokit\LatLng($this->getParams["lat"], $this->getParams["lng"]);
    }
    
    public function getAzimuthRequestAsClock() {
        return PNUtil::degreeToClock($this->getParams["azimuth"]);
    }
  
    public function getPitchRequestAsClock() {
        return PNUtil::degreeToClock($this->getParams["pitch"]);
    }

    public function getRollRequestAsClock() {
        return PNUtil::degreeToClock($this->getParams["roll"]);
    }
    
    private function getBBoxStringFromPositionRequest($radius) {
        $position =  $this->getPositionRequest();
        $box = PNUtil::$math->expand($position, $radius . 'm');
        return $box->getSouthWest() . ",". $box->getNorthEast();
    }
    
    public function getOSMData($request, $radius) {
        // create a bounding box from the given position
        
        $box_str = $this->getBBoxStringFromPositionRequest($radius);
        
        // build request
        $overpass = 'http://overpass-api.de/api/interpreter?data=' . PigeonNelsonServer::replaceBoxInRequest($request, $box_str);
        
        
        // collecting results in JSON format
        $html = file_get_contents($overpass);
        $result = json_decode($html, true); // "true" to get PHP array instead of an object

        // set internal data
        $this->data = $result['elements'];

    }
    
    public function hasEntries() {
        return $this->data != null && count($this->data) != 0;
    }
    
    public function getEntries() {
        return $this->data;
    }
    
    
    public function clearMessages() {
        $this->messages = [];
    }
    
    public function addMessage($message) {
        if (!$message->hasForgettingConditionTFR() && isset($this->defaultPeriodBetweenUpdates) && $this->defaultPeriodBetweenUpdates > 0)
            $message->addForgettingConditionTimeFromReception($this->defaultPeriodBetweenUpdates);
        array_push($this->messages, $message);
    }
    
    public function printMessages() {
        print "[";
        
        $first = true;
        foreach($this->messages as $message) {
            if ($first)
                $first = false;
            else
                print ", ";
            print $message->toString();
        }
        
        
        print "]";
    }
    

};


?>

