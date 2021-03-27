package fr.lepigeonnelson.player.broadcastplayer.messages;

import android.location.Location;

import fr.lepigeonnelson.player.broadcastplayer.SensorsService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistanceToCondition implements MessageCondition {

    private SensorsService locationService;
    private Location refLocation;
    private float distance;
    private Maths.Comparison comparison;

    public DistanceToCondition(String reference, Maths.Comparison c, float parameter) {
        Pattern p = Pattern.compile("^distanceTo[(][ ]*([+-]?\\d*\\.?\\d*),[ ]*([+-]?\\d*\\.?\\d*)[ ]*[)]$");
        Matcher m = p.matcher(reference);
        refLocation = null;
        if (m.find()) {
            float latitude = Float.parseFloat(m.group(1));
            float longitude = Float.parseFloat(m.group(2));
            refLocation = new Location("Reference point");
            refLocation.setLatitude(latitude);
            refLocation.setLongitude(longitude);
        }
        this.comparison = c;
        this.distance = parameter;
        this.locationService = SensorsService.getSensorsService();
    }

    @Override
    public boolean satisfied(BMessage message) {
        if (refLocation == null)
            return false;
        else {
            Location l = locationService.getLocation();
            float d = l.distanceTo(refLocation);
            return Maths.compare(d, comparison, distance);
        }
    }

    @Override
    public boolean isTimeConstraint() {
        return false;
    }
}
