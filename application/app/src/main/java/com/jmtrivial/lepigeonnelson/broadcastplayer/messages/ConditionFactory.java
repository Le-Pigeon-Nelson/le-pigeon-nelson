package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public class ConditionFactory {


    public MessageCondition getCondition(String refVariable,
                                         String comparison,
                                         String parameter,
                                         boolean reverse) {
        Maths.Comparison c = Maths.getComparisonFromString(comparison);
        if (refVariable == null || c == Maths.Comparison.unknown)
            return null;

        if (reverse) {
            c = Maths.reverseComparison(c);
        }

        if (refVariable == "timeFromReception") {
            return new TimeFromReceptionCondition(c, Integer.parseInt(parameter));
        }
        return null;
    }



}
