package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

import android.location.Location;
import android.util.Log;

import com.jmtrivial.lepigeonnelson.broadcastplayer.SensorsService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzimuthDeviationCondition implements MessageCondition {
    private SensorsService locationService;
    private Float refBearing;
    private float angle;
    private Maths.Comparison comparison;

    public AzimuthDeviationCondition(String reference, Maths.Comparison c, float parameter) {
        Pattern p = Pattern.compile("^azimuthDeviation[(][ ]*([+-]?\\d*\\.?\\d*)[ ]*[)]$");
        Matcher m = p.matcher(reference);
        refBearing = null;
        if (m.find()) {
            refBearing = Float.parseFloat(m.group(1));
        }
        this.comparison = c;
        this.angle = parameter;
        this.locationService = SensorsService.getSensorsService();
    }

    @Override
    public boolean satisfied(BMessage message) {
        if (refBearing == null)
            return false;
        else {
            float azimuth = locationService.getAzimuth();
            Log.d("azimuthDeviation", "angle: " + azimuth);
            float angleBetweenAzimuths = Math.abs(normalizeDegree(azimuth - refBearing));
            return Maths.compare(angleBetweenAzimuths, comparison, angle);
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
