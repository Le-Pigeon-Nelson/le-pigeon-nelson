package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public class PitchDeviationCondition extends AngleDeviationCondition {
    public PitchDeviationCondition(String reference, Maths.Comparison c, float parameter) {
        super("pitch", reference, c, parameter);
    }
}
