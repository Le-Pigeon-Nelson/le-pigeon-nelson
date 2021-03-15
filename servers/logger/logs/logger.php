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
        return $this->epochToString($this->start) . " => ". $this->epochToString($this->end) ." (". $this->nbRecordings . " recordings)";
    }
    
    public static function epochToString($date) {
        return date("d/m/Y H:i:s", $date);
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
        return $this->getParameter("lat");
    }
    public function getLng() {
        return $this->getParameter("lng");
    }
    public function getAccuracy() {
        return $this->getParameter("loc_accuracy");
    }
    public function getAzimuth() {
        return $this->getParameter("azimuth");
    }
    public function getPitch() {
        return $this->getParameter("azimuth");
    }
    public function getGPSTimestamp() {
        return $this->getParameter("loc_timestamp");
    }
    public function getRoll() {
        return $this->getParameter("roll");
    }
    
    public function getGPSTimestampToString() {
        if (array_key_exists("loc_timestamp", $this->parameters))
            return SeriesDescription::epochToString($this->getGPSTimestamp()/1000);
        else
            return "N/A";
    }

    
    public function getParameter($key) {
        if (array_key_exists($key, $this->parameters))
            return $this->parameters[$key];
        else
            return "N/A";
    }
    
    public function toHTML() {
        $result = "<h4>" . SeriesDescription::epochToString($this->timestamp) . "</h4>";
        $result .= "<strong>GPS timestamp:</strong> ". $this->getGPSTimestampToString() . "<br />";
        $result .= "<strong>coords:</strong> ". $this->getLat(). ", " . $this->getLng() . "<br />";
        $result .= "<strong>accuracy:</strong> " . $this->getAccuracy() . " meters <br />";
        $result .= "<strong>azimuth:</strong> " . $this->getAzimuth() . " degrees<br />";
        $result .= "<strong>roll:</strong> " . $this->getRoll() . " degrees<br />";
        $result .= "<strong>pitch:</strong> " . $this->getPitch() . " degrees<br />";
        
        return $result;
    }
    
    public function toHTMLArray() {
        $result = "<tr><th>" .  SeriesDescription::epochToString($this->timestamp). "</th>";
        $result .= "<td>" .  $this->getGPSTimestampToString() . "</td>";
        $result .= "<td>" . $this->getLat(). "</td>";
        $result .= "<td>" . $this->getLng() . "</td>";
        $result .= "<td>" . $this->getAccuracy() . "</td>";
        $result .= "<td>" . $this->getAzimuth()  . "</td>";
        $result .= "<td>" . $this->getRoll()  . "</td>";
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
        $commands = [ 'CREATE TABLE IF NOT EXISTS parameters (
                            uid TEXT NOT NULL,
                            timestamp INTEGER NOT NULL,
                            key TEXT NOT NULL,
                            value TEXT NOT NULL)',
                        'CREATE TABLE IF NOT EXISTS series (
                            uid TEXT NOT NULL,
                            begin INTEGER NOT NULL,
                            end INTEGER NOT NULL,
                            nbEntries INTEGER NOT NULL)',
                            'CREATE TABLE IF NOT EXISTS pred (
                            uid TEXT NOT NULL,
                            timestamp INTEGER NOT NULL,
                            pred_ts INTEGER NOT NULL)'
                            
                        ];
    
        foreach ($commands as $command) {
            $this->db->exec($command);
        }
        
    }

    public function getPreviousEntry($uid, $timestamp) {
        $command = "SELECT DISTINCT max(timestamp) AS max FROM parameters WHERE uid = '". 
        SQLite3::escapeString($uid) . "' AND timestamp < " . $timestamp;
        
        $results = $this->db->query($command);
        $row = $results->fetchArray();
        if (array_key_exists("max", $row) && $row["max"] != "")
            return $row["max"];
        else
            return 0;
 
    
    }
    
    public function log($entries) {
        $timestamp = time();
        if (!array_key_exists("uid", $entries))
            return false;
            
        $uid = $entries["uid"];
        
        // find previous entry
        $previous = $this->getPreviousEntry($uid, $timestamp);
        
        // store previous timestamp
        $command = "INSERT INTO pred(uid, timestamp, pred_ts) 
                VALUES('" . SQLite3::escapeString($uid) . "', 
                ". $timestamp . ",
                " . $previous  .")";

        $this->db->exec($command);
        
        // save entries
        foreach($entries as $key => $value) {
            if (strcmp($key, "uid") != 0) {
                $command = "INSERT INTO parameters(uid, timestamp, key, value) 
                VALUES('" . SQLite3::escapeString($uid) . "', 
                ". $timestamp . ", 
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
    
    private function getSeriesBegins($uid, $interval, $date) {
        $command = "SELECT DISTINCT parameters.uid, parameters.timestamp FROM parameters, pred WHERE ";
        $command .= " parameters.uid = pred.uid AND parameters.timestamp = pred.timestamp ";
        if ($date != NULL)
            $command .= " AND parameters.timestamp > " . $date;
            
        if ($uid != NULL) {
            $command .= " AND parameters.uid = '" . $uid . "'";
        }

        $command .= " AND parameters.timestamp - " . $interval . " > pred.pred_ts";


        
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
        $command = "SELECT DISTINCT parameters.uid, parameters.timestamp FROM parameters ";
        $command .= " LEFT JOIN pred ON parameters.uid = pred.uid AND parameters.timestamp = pred.pred_ts ";
        $command .= " WHERE ";
        if ($date != NULL)
            $command .= " parameters.timestamp > " . $date . " AND ";
                
        if ($uid != NULL) {
            $command .= " parameters.uid = '" . $uid . "' AND ";
        }
        

        $command .= "(parameters.timestamp + " . $interval . " < pred.timestamp OR pred.uid IS NULL)";

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
                AND timestamp >= " . $description->start . " 
                AND timestamp <= " . $description->end;
        $result = array();
        $results = $this->db->query($command);
        $row = $results->fetchArray();
        $description->setNbRecordings($row["nb"]);
    }
    
    
    public function loadSeriesDescriptions($uid) {
        $result = [];
        
        $command = "SELECT uid, begin, end, nbEntries FROM series";
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
        $command = "INSERT INTO series(uid, begin, end, nbEntries) 
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
        $command = "select * from parameters where uid = '" . $desc->uid . "'
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
        $command = "DELETE FROM series";
        $this->db->exec($command);
        echo "<p>Rebuilding database...</p>";
        $this->getSeriesDescriptions(NULL, $interval);
    }
    
}

?>

