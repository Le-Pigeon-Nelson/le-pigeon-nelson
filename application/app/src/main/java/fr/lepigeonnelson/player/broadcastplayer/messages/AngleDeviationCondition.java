package fr.lepigeonnelson.player.broadcastplayer.messages;

import android.util.Log;

import fr.lepigeonnelson.player.broadcastplayer.SensorsService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AngleDeviationCondition implements MessageCondition {

    private SensorsService locationService;
    private Float refBearing;
    private float angle;
    private Maths.Comparison comparison;

    protected String name;

    protected AngleDeviationCondition(String name, String reference, Maths.Comparison c, float parameter) {
        this.name = name;
        Pattern p = Pattern.compile("^" + name + "Deviation[(][ ]*([+-]?\\d*\\.?\\d*)[ ]*[)]$");
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
            float value = locationService.getAzimuth();
            Log.d(name + "Deviation", "angle: " + value);
            float angleBetweenAngles = Math.abs(normalizeDegree(value - refBearing));
            return Maths.compare(angleBetweenAngles, comparison, angle);
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
