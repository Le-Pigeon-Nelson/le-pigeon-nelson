package fr.lepigeonnelson.player.broadcastplayer.messages;

public class Maths {
    public enum Comparison {smallerThan, greaterThan, smallerOrEqualTo, greaterOrEqualTo, unknown };

    public static boolean compare(float a, Comparison comparison, float b) {
        if (comparison == Comparison.smallerThan) {
            return a < b;
        }
        else if (comparison == Comparison.greaterThan) {
            return a > b;
        }
        else if (comparison == Comparison.smallerOrEqualTo) {
            return a <= b;
        }
        else if (comparison == Comparison.greaterOrEqualTo) {
            return a >= b;
        }
        else {
            return false;
        }
    }
    public static boolean compare(long a, Comparison comparison, long b) {
        if (comparison == Comparison.smallerThan) {
            return a < b;
        }
        else if (comparison == Comparison.greaterThan) {
            return a > b;
        }
        else if (comparison == Comparison.smallerOrEqualTo) {
            return a <= b;
        }
        else if (comparison == Comparison.greaterOrEqualTo) {
            return a >= b;
        }
        else {
            return false;
        }
    }



    static public Comparison reverseComparison(Comparison c) {
        if (c == Comparison.smallerThan)
            return Comparison.greaterThan;
        else if (c == Comparison.greaterThan)
            return Comparison.smallerThan;
        else if (c == Comparison.smallerOrEqualTo)
            return Comparison.greaterOrEqualTo;
        else if (c == Comparison.greaterOrEqualTo)
            return Comparison.smallerOrEqualTo;
        else
            return Comparison.unknown;
    }

    static public Comparison getComparisonFromString(String comparison) {
        if (comparison == null) {
            return Comparison.unknown;
        }
        else if (comparison.equals("smallerThan") || comparison.equals("<") || comparison.equals("lessThan")) {
            return Comparison.smallerThan;
        }
        else if(comparison.equals("smallerOrEqualTo") || comparison.equals("<=") || comparison.equals("lessOrEqualTo")) {
            return Comparison.smallerOrEqualTo;
        }
        else if (comparison.equals("greaterThan") || comparison.equals(">")) {
            return Comparison.greaterThan;
        }
        else if(comparison.equals("greaterOrEqualTo") || comparison.equals(">=")) {
            return Comparison.greaterOrEqualTo;
        }
        else {
            return Comparison.unknown;
        }
    }

}
