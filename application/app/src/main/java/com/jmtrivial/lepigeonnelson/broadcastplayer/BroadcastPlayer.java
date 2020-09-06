package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Message;

public class BroadcastPlayer extends HandlerThread {

    private int refreshDelay;
    private MessageCollector messageCollector;
    private MessagePlayer messagePlayer;
    private MessageQueue messageQueue;

    private Server server;

    private Context context;

    public BroadcastPlayer(Context context, int refreshDelay) {
        super("BroadcastPlayer");
        this.context = context;
        this.refreshDelay = refreshDelay;
        LocationService.getLocationManager(context).register(this);
        messagePlayer = null;
        messageCollector = null;
        messageQueue = null;
    }

    @Override
    protected void onLooperPrepared() {
        messagePlayer = new MessagePlayer(context);
        messageQueue = new MessageQueue(messagePlayer, refreshDelay);
        messageCollector = new MessageCollector(messageQueue, context);
    }

    public void playBroadcast() {
        if (messageCollector != null) {
            messageCollector.setServer(server);
            messageQueue.setServerPeriod(server.getPeriod());
            Message msg = messageCollector.obtainMessage();
            msg.obj = server;
            msg.what = messageCollector.startCollect;
            messageCollector.sendMessage(msg);
        }
    }

    public void stopBroadcast() {
        if (messageCollector != null && messageQueue != null) {
            messageCollector.sendEmptyMessage(messageCollector.stopCollect);
            messageQueue.sendEmptyMessage(messageQueue.stopBroadcast);
        }
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return this.server;
    }

    public void locationChanged() {
        // if location has changed, try if a message can be played
        if (messageQueue != null) {
            messageQueue.sendEmptyMessage(messageQueue.checkForPlayableMessage);
        }
    }

    public void reset() {
        if (messageQueue != null) {
            messagePlayer.reset();
        }
    }
}
