package com.jmtrivial.lepigeonnelson.broadcastplayer.messages;

public class MessageCondition {

    private Integer lifespan;

    public MessageCondition(Integer lifespan) {
        this.lifespan = lifespan;
        // TODO: implement other message conditions
    }

    public boolean satisfied(BMessage message) {
        // TODO: implement satisfaction test
        return true;
    }
}
