package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public class Maths {
    public enum Comparison {smallerThan, greaterThan, smallerOrEqualTo, greaterOrEqualTo, unknown };


    public static boolean compare(long a, Comparison comparison, long b) {
        if (comparison == Comparison.smallerThan)
            return a < b ;
        else if (comparison == Comparison.greaterThan)
            return a > b;
        else if (comparison == Comparison.smallerOrEqualTo)
            return a <= b;
        else if (comparison == Comparison.greaterOrEqualTo)
            return a >= b;
        else
            return false;
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
        if (comparison == null)
            return Comparison.unknown;
        if (comparison == "smallerThan" || comparison == "<") {
            return Comparison.smallerThan;
        }
        else if(comparison == "smallerOrEqualTo" || comparison == "<=") {
            return Comparison.smallerOrEqualTo;
        }
        else if (comparison == "greaterThan" || comparison == ">") {
            return Comparison.greaterThan;
        }
        else if(comparison == "greaterOrEqualTo" || comparison == ">=") {
            return Comparison.greaterOrEqualTo;
        }
        else
            return Comparison.unknown;
    }

}
