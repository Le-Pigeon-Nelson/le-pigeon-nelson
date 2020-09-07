package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

import android.location.Location;

import com.jmtrivial.lepigeonnelson.broadcastplayer.LocationService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AngularDeviationCondition implements MessageCondition {
    private LocationService locationService;
    private Float refBearing;
    private float angle;
    private Maths.Comparison comparison;

    public AngularDeviationCondition(String reference, Maths.Comparison c, float parameter) {
        Pattern p = Pattern.compile("^angularDeviation[(][ ]*([+-]?\\d*\\.?\\d*)[ ]*[)]$");
        Matcher m = p.matcher(reference);
        refBearing = null;
        if (m.find()) {
            refBearing = Float.parseFloat(m.group(1));
        }
        this.comparison = c;
        this.angle = parameter;
        this.locationService = LocationService.getLocationManager();
    }

    @Override
    public boolean satisfied(BMessage message) {
        if (refBearing == null)
            return false;
        else {
            Location l = locationService.location;
            float b = l.getBearing();
            float angleBetweenBearings = Math.abs(normalizeDegree(b - refBearing));
            return Maths.compare(angleBetweenBearings, comparison, angle);
        }
    }

    private static float normalizeDegree(float value) {
        if(value >= 180.0){
            return value - 360;
        } else {
            return value;
        }
    }

    @Override
    public boolean isTimeConstraint() {
        return false;
    }
}
