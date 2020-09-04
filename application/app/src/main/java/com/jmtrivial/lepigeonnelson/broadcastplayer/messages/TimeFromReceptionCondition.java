package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

import android.os.SystemClock;

public class TimeFromReceptionCondition implements MessageCondition {

    private int parameterMs;
    private Maths.Comparison comparison;

    public TimeFromReceptionCondition(Maths.Comparison c, int parameter) {
        this.comparison = c;
        this.parameterMs = parameter * 1000;
    }

    @Override
    public boolean satisfied(BMessage message) {
        long timeFromReception = SystemClock.currentThreadTimeMillis() - message.getCollectedTime();
        return Maths.compare(timeFromReception, comparison, parameterMs);
    }
}
