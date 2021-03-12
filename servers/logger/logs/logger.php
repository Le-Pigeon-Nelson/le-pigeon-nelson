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
    
    public function toString() {
        return $this->jdToString($this->start) . " => ". $this->jdToString($this->end) ." (". $this->nbRecordings . " recordings)";
    }
    
    public static function jdToString($date) {
        $shift = 11; /* fix halfday*/
    
        $start = $date /( 24*60*60);
        $startInt = intval($start);
        $diff = $date + ($shift - $startInt * 24) * 60 * 60;
        $unix = jdtounix($start) + $diff;
        return date("d/m/Y H:i:s", $unix);
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
    public function getAzimuth() {
        return $this->parameters["azimuth"];
    }
    public function getPitch() {
        return $this->parameters["azimuth"];
    }
    
    public function toHTML() {
        $result = "<h4>" . SeriesDescription::jdToString($this->timestamp) . "</h4>";
        $result .= "<strong>coords:</strong> ". $this->getLat(). ", " . $this->getLng() . "<br />";
        $result .= "<strong>accuracy:</strong> " . $this->getAccuracy() . " meters <br />";
        $result .= "<strong>azimuth:</strong> " . $this->getAzimuth() . " degrees<br />";
        $result .= "<strong>roll:</strong> " . $this->parameters["roll"] . " degrees<br />";
        $result .= "<strong>pitch:</strong> " . $this->getPitch() . " degrees<br />";
        
        return $result;
    }
    
    public function toHTMLArray() {
        $result = "<tr><th>" .  SeriesDescription::jdToString($this->timestamp) . "</th>";
        $result .= "<td>" . $this->getLat(). "</td>";
        $result .= "<td>" . $this->getLng() . "</td>";
        $result .= "<td>" . $this->getAccuracy() . "</td>";
        $result .= "<td>" . $this->getAzimuth()  . "</td>";
        $result .= "<td>" . $this->parameters["roll"]  . "</td>";
        $result .= "<td>" . $this->getPitch() . "</td>";
        $result .= "</tr>";
        
        return $result;
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
        $commands = [ 'CREATE TABLE IF NOT EXISTS parameters_intsec (
                            uid TEXT NOT NULL,
                            timestamp INTEGER NOT NULL,
                            key TEXT NOT NULL,
                            value TEXT NOT NULL)',
                        'CREATE TABLE IF NOT EXISTS series_insec (
                            uid TEXT NOT NULL,
                            begin INTEGER NOT NULL,
                            end INTEGER NOT NULL,
                            nbEntries INTEGER NOT NULL)' ];
    
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
                $command = "INSERT INTO parameters_intsec(uid, timestamp, key, value) 
                VALUES('" . SQLite3::escapeString($uid) . "', 
                CAST(JulianDay('"  . SQLite3::escapeString($timestamp) . "') * 24 * 60 * 60 as INTEGER), 
                '"  . SQLite3::escapeString($key) . "', 
                '"  . SQLite3::escapeString($value) . "')";
                $this->db->exec($command);
            }
        }
        
        return true;
        
    }
    
    public function getUIDs() {
        $command = "SELECT DISTINCT uid FROM parameters_intsec";
        $result = [];
        
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            array_push($result, $row["uid"]);
        }
        
        return $result;
    
    }
    
    private function getSeriesBegins($uid, $interval, $date) {
        $command = "SELECT DISTINCT param1.uid, param1.timestamp FROM parameters_intsec as param1 WHERE ";
        if ($date != NULL)
            $command .= "param1.timestamp > " . $date . " AND ";
            
        if ($uid != NULL) {
            $command .= " param1.uid = '" . $uid . "' AND ";
        }

        $command .= "(param1.timestamp - " . $interval . " >
        (SELECT max(param2.timestamp) FROM parameters_intsec as param2 WHERE param1.uid = param2.uid AND param2.timestamp < param1.timestamp)
        OR param1.timestamp <= (SELECT min(param2.timestamp) FROM parameters_intsec as param2 WHERE param1.uid = param2.uid)) ORDER BY param1.timestamp";


        
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
    
    private function getSeriesEnds($uid, $interval, $date) {
        $command = "SELECT DISTINCT param1.uid, param1.timestamp FROM parameters_intsec as param1 WHERE ";
        if ($date != NULL)
            $command .= "param1.timestamp > " . $date . " AND ";
                
        if ($uid != NULL) {
            $command .= " param1.uid = '" . $uid . "' AND ";
        }

        $command .= "(param1.timestamp + " . $interval . " <
        (SELECT min(param2.timestamp) FROM parameters_intsec as param2 WHERE param1.uid = param2.uid AND param2.timestamp > param1.timestamp)
        OR param1.timestamp >= (SELECT max(param2.timestamp) FROM parameters_intsec as param2 WHERE param1.uid = param2.uid)) ORDER BY param1.timestamp";

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
    
        $command = "SELECT COUNT(DISTINCT timestamp) AS nb FROM parameters_intsec WHERE uid = '" . $description->uid ."'
                AND timestamp >= " . $description->start . " 
                AND timestamp <= " . $description->end;
        $result = array();
        $results = $this->db->query($command);
        $row = $results->fetchArray();
        $description->setNbRecordings($row["nb"]);
    }
    
    
    public function loadSeriesDescriptions($uid) {
        $result = [];
        
        $command = "SELECT uid, begin, end, nbEntries FROM series_insec";
        if ($uid != NULL)
            $command .= " WHERE uid = '" . $uid . "'";
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            $description = new SeriesDescription($row["uid"], $row["begin"], $row["end"]);
            $description->setNbRecordings($row["nbEntries"]);
            array_push($result, $description);
        }
        
        return $result;
    }
    
    public function storeSeriesDescription($desc) {
        $command = "INSERT INTO series_insec(uid, begin, end, nbEntries) 
                VALUES('" . SQLite3::escapeString($desc->uid) . "', 
                "  . $desc->start . ", 
                "  . $desc->end . ", 
                "  . $desc->nbRecordings . ")";
        $this->db->exec($command);
    }
    
    public function getSeriesDescriptions($uid = NULL, $interval) {
        $result = $this->loadSeriesDescriptions($uid);
        
        $start = NULL;
        foreach($result as $desc) {
            if ($start == NULL || $start < $desc->end) {
                $start = $desc->end;
            }
        }
    
        $begins = $this->getSeriesBegins($uid, $interval, $start);
        $ends = $this->getSeriesEnds($uid, $interval, $start);
        
        
        foreach($begins as $luid => $beginTSs) {
            foreach($beginTSs as $id => $beginTS) {
                $endTS = $ends[$luid][$id];
                $description = new SeriesDescription($luid, $beginTS, $endTS);
                $this->setNbRecordings($description);
                $this->storeSeriesDescription($description);
                array_push($result, $description);
                
            }
        }
        
        return $result;
        
    }
    
    public function getSeries($desc) {
        $command = "select * from parameters_intsec where uid = '" . $desc->uid . "'
                AND timestamp >= " . $desc->start . " 
                AND timestamp <= " . $desc->end;
        $result = new Series();
        $results = $this->db->query($command);
        while ($row = $results->fetchArray()) {
            $result->addParam($row["uid"], $row["timestamp"], $row["key"], $row["value"]);
        }
        
        return $result;
    }   
    
    public function rebuildSeriesDescriptions($interval) {
        $command = "DELETE FROM series_insec";
        $this->db->exec($command);
        echo "<p>Rebuilding database...</p>";
        $this->getSeriesDescriptions(NULL, $interval);
    }
    
}

?>

