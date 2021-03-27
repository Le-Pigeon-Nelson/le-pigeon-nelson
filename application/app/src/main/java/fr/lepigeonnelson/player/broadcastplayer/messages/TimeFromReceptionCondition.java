package fr.lepigeonnelson.player.broadcastplayer.messages;

import java.util.Date;

public class TimeFromReceptionCondition implements MessageCondition {

    private int parameterMs;
    private Maths.Comparison comparison;

    public TimeFromReceptionCondition(Maths.Comparison c, int parameter) {
        this.comparison = c;
        this.parameterMs = parameter * 1000;
    }

    @Override
    public boolean satisfied(BMessage message) {
        Date d = new Date();
        long timeFromReception = d.getTime() - message.getCollectedTime();
        return Maths.compare(timeFromReception, comparison, parameterMs);
    }

    @Override
    public boolean isTimeConstraint() {
        return true;
    }
}
