<?php


class SeriesDescription {
    public function __construct($uid, $start, $end) {
        $this->uid = $uid;
        $this->start = $start;
        $this->end = $end;
    
    }
    public function setNbRecordings($nb) {
        $this->nbRecordings = $nb;
    }

}

class Entry {

    public function __construct($uid, $timestamp) {
        $this->uid = $uid;
        $this->timestamp = $timestamp;
        $this->parameters = array();
    }
    
    public function addParam($key, $value) {
        $this->parameters[$key] = $value;
    }
    
    public function getLat() {
        return $this->parameters["lat"];
    }
    public function getLng() {
        return $this->parameters["lng"];
    }
    public function getAccuracy() {
        return $this->parameters["loc_accuracy"];
    }

}

class Series {

    public function __construct() {
        $this->entries = array();
    }
    
    public function addParam($uid, $timestamp, $key, $value) {
        if (!array_key_exists($timestamp, $this->entries)) {
            $this->entries[$timestamp] = new Entry($uid, $timestamp);
        }
        $this->entries[$timestamp]->addParam($key, $value);
    }
    

}

class Logger {

    public function __construct() {
        $this->db = new SQLite3(__DIR__ .'/logs.sqlite');
        
        $this->createTablesIfNotExist();
    }
    
    private function createTablesIfNotExist() {
        $commands = [ 'CREATE TABLE IF NOT EXISTS parameters (
                            uid TEXT NOT NULL,
                            timestamp TEXT NOT NULL,
                            key TEXT NOT NULL,
                            value TEXT NOT NULL)' ];
    
        foreach ($commands as $command) {
            $this->db->exec($command);
        }
    
    }

    public function log($entries) {
        $timestamp = date('Y-m-d H:i:s');
        if (!array_key_exists("uid", $entries))
            return false;
            
        $uid = $entries["uid"];
        
        foreach($entries as $key => $value) {
            if (strcmp($key, "uid") != 0) {
                $command = "INSERT INTO parameters(uid, timestamp, key, value) 
                VALUES('" . SQLite3::escapeString($uid) . "', 
                '"  . SQLite3::escapeString($timestamp) . "', 
                '"  . SQLite3::escapeString($key) . "', 
                '"  . SQLite3::escapeString($value) . "')";
                $this->db->exec($command);
            }
        }
        
        return true;
        
    }
    
    public function getUIDs() {
        $command = "SELECT DISTINCT uid FROM parameters";
        $result = [];
        
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            array_push($result, $row["uid"]);
        }
        
        return $result;
    
    }
    
    private function getSeriesBegins($uid, $interval) {
        $command = "SELECT DISTINCT param1.uid, param1.timestamp FROM parameters as param1 WHERE ";
                
        if ($uid != NULL) {
            $command .= " param1.uid = '" . $uid . "' AND ";
        }

        $command .= "(JulianDay(param1.timestamp) * 24 * 60 * 60 - " . $interval . " >
        (SELECT max(JulianDay(param2.timestamp) * 24 * 60 * 60) FROM parameters as param2 WHERE param1.uid = param2.uid AND JulianDay(param2.timestamp) < JulianDay(param1.timestamp))
        OR JulianDay(param1.timestamp) <= (SELECT min(JulianDay(param2.timestamp)) FROM parameters as param2 WHERE param1.uid = param2.uid)) ORDER BY param1.timestamp";


        
        $result = array();
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            if (!array_key_exists($row["uid"], $result)) {
                $result[$row["uid"]] = [];
            }
            array_push($result[$row["uid"]], $row["timestamp"]);
        }
        
        return $result;
        
    }
    
    private function getSeriesEnds($uid, $interval) {
        $command = "SELECT DISTINCT param1.uid, param1.timestamp FROM parameters as param1 WHERE ";
                
        if ($uid != NULL) {
            $command .= " param1.uid = '" . $uid . "' AND ";
        }

        $command .= "(JulianDay(param1.timestamp) * 24 * 60 * 60 + " . $interval . " <
        (SELECT min(JulianDay(param2.timestamp) * 24 * 60 * 60) FROM parameters as param2 WHERE param1.uid = param2.uid AND JulianDay(param2.timestamp) > JulianDay(param1.timestamp))
        OR JulianDay(param1.timestamp) >= (SELECT max(JulianDay(param2.timestamp)) FROM parameters as param2 WHERE param1.uid = param2.uid)) ORDER BY param1.timestamp";

        $result = array();
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            if (!array_key_exists($row["uid"], $result)) {
                $result[$row["uid"]] = [];
            }
            array_push($result[$row["uid"]], $row["timestamp"]);
        }

        
        return $result;
        
    }
    
    private function setNbRecordings($description) {
    
        $command = "SELECT COUNT(DISTINCT timestamp) AS nb FROM parameters WHERE uid = '" . $description->uid ."'
                AND JulianDay(parameters.timestamp) >= JulianDay('" . $description->start . "') 
                AND JulianDay(parameters.timestamp) <= JulianDay('" . $description->end . "')";
        $result = array();
        $results = $this->db->query($command);
        $row = $results->fetchArray();
        $description->setNbRecordings($row["nb"]);
    }
    
    
    public function getSeriesDescriptions($uid = NULL, $interval) {
        $result = [];
    
    
        $begins = $this->getSeriesBegins($uid, $interval);
        $ends = $this->getSeriesEnds($uid, $interval);
        
        
        foreach($begins as $luid => $beginTSs) {
            foreach($beginTSs as $id => $beginTS) {
                $endTS = $ends[$luid][$id];
                $description = new SeriesDescription($luid, $beginTS, $endTS);
                $this->setNbRecordings($description);
                array_push($result, $description);
            }
        }
        
        return $result;
        
    }
    
    public function getSeries($desc) {
        $command = "select * from parameters where uid = '" . $desc->uid . "'
                AND JulianDay(parameters.timestamp) >= JulianDay('" . $desc->start . "') 
                AND JulianDay(parameters.timestamp) <= JulianDay('" . $desc->end . "')";
        $result = new Series();
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            $result->addParam($row["uid"], $row["timestamp"], $row["key"], $row["value"]);
        }
        
        return $result;
    }   
    
}

?>

