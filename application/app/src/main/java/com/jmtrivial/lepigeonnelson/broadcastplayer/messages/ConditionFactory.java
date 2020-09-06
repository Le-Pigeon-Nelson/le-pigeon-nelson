package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

import android.util.Log;

public class ConditionFactory {


    public MessageCondition getCondition(String reference,
                                         String comparison,
                                         String parameter,
                                         boolean reverse) {
        Maths.Comparison c = Maths.getComparisonFromString(comparison);
        if (reference == null || c == Maths.Comparison.unknown) {
            return null;
        }

        if (reverse) {
            c = Maths.reverseComparison(c);
        }

        if (reference.equals("timeFromReception")) {
            if (parameter == null)
                return null;
            return new TimeFromReceptionCondition(c, Integer.parseInt(parameter));
        }
        else if (reference.matches("^distanceTo[(][ ]*[+-]?\\d*\\.?\\d*,[ ]*[+-]?\\d*\\.?\\d*[ ]*[)]$")) {
            return new DistanceToCondition(reference, c, Float.parseFloat(parameter));
        }
        else {
            Log.d("MessageCondition", "Unknown reference: " + reference);
        }
        return null;
    }



}
