package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public class RollDeviationCondition extends AngleDeviationCondition {
    public RollDeviationCondition(String reference, Maths.Comparison c, float parameter) {
        super("roll", reference, c, parameter);
    }
}
