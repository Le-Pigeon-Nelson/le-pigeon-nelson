package fr.lepigeonnelson.player.broadcastplayer.messages;

public interface MessageCondition {


    boolean satisfied(BMessage message);

    boolean isTimeConstraint();

}
