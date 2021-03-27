package fr.lepigeonnelson.player.broadcastplayer.messages;

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
        else if  (reference.matches("^azimuthDeviation[(][ ]*[+-]?\\d*\\.?\\d*[ ]*[)]$")) {
            Log.d("Condition factory", "azimuthDeviation");
            return new AzimuthDeviationCondition(reference, c, Float.parseFloat(parameter));
        }
        else if  (reference.matches("^pitchDeviation[(][ ]*[+-]?\\d*\\.?\\d*[ ]*[)]$")) {
            Log.d("Condition factory", "pitchDeviation");
            return new PitchDeviationCondition(reference, c, Float.parseFloat(parameter));
        }
        else if  (reference.matches("^rollDeviation[(][ ]*[+-]?\\d*\\.?\\d*[ ]*[)]$")) {
            Log.d("Condition factory", "rollDeviation");
            return new RollDeviationCondition(reference, c, Float.parseFloat(parameter));
        }
        else {
            Log.d("MessageCondition", "Unknown reference: " + reference);
        }
        return null;
    }



}
