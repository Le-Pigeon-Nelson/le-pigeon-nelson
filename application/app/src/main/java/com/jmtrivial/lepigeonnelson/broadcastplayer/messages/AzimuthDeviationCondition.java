package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public class AzimuthDeviationCondition extends AngleDeviationCondition {

    public AzimuthDeviationCondition(String reference, Maths.Comparison c, float parameter) {
        super("azimuth", reference, c, parameter);

    }

}
