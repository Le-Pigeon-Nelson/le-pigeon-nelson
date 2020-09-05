package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public interface MessageCondition {


    boolean satisfied(BMessage message);

    boolean isTimeConstraint();

}
