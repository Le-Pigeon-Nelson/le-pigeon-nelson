package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class BroadcastPlayer extends HandlerThread {

    private final Activity activity;
    private int refreshDelay;
    private MessageCollector messageCollector;
    private MessagePlayer messagePlayer;
    private MessageQueue messageQueue;


    private UIHandler uiHandler;

    private ServerDescription currentServer;

    private Context context;
    private boolean working;

    public BroadcastPlayer(Activity activity, int refreshDelay, UIHandler uiHandler) {
        super("BroadcastPlayer");
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.refreshDelay = refreshDelay;
        SensorsService.getSensorsService(activity).register(this);
        messagePlayer = null;
        messageCollector = null;
        messageQueue = null;
        working = false;



        this.uiHandler = uiHandler;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onLooperPrepared() {
        messagePlayer = new MessagePlayer(context);
        messageQueue = new MessageQueue(messagePlayer, refreshDelay, uiHandler);
        messageCollector = new MessageCollector(messageQueue, activity, uiHandler);
    }

    public void playBroadcast() {
        if (messageCollector != null) {
            messageCollector.setCurrentServer(currentServer);
            messageQueue.setServerPeriod(currentServer.getPeriod());
            Message msg = messageCollector.obtainMessage();
            msg.obj = currentServer;
            msg.what = messageCollector.startCollect;
            messageCollector.sendMessage(msg);
            working = true;
        }
    }

    public void stopBroadcast() {
        if (messageCollector != null && messageQueue != null) {
            messageCollector.sendEmptyMessage(messageCollector.stopCollect);
            messageQueue.sendEmptyMessage(messageQueue.stopBroadcast);
        }
        working = false;
    }

    public void setCurrentServer(ServerDescription currentServer) {
        this.currentServer = currentServer;
    }

    public ServerDescription getCurrentServer() {
        return this.currentServer;
    }

    public void locationChanged() {
        // if location has changed, try if a message can be played
        if (messageQueue != null) {
            messageQueue.sendEmptyMessage(messageQueue.checkForPlayableMessage);
        }
    }

    public void reset() {
        if (messagePlayer != null) {
            messagePlayer.reset();
        }
    }

    public boolean isWorking() {
        return working;
    }

    public void setListener(BroadcastPlayerListener listener) {
        uiHandler.setListener(listener);
    }

    public void checkSensorsSettings() {
        SensorsService service = SensorsService.getSensorsService(activity);
        if (service != null) {
            service.checkSensorsSettings();
        }
    }

    public void collectServerDescription(final ServerDescription serverDescription) {
        if (serverDescription.isSelfDescribed()) {
            if (messageCollector != null) {
                Message msg = messageCollector.obtainMessage();
                msg.obj = serverDescription;
                msg.what = messageCollector.getDescription;
                messageCollector.sendMessage(msg);
            }
            else  {
                // delay
                Log.d("BroadcastPlayer", "collector not ready, wait 1 second");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        collectServerDescription(serverDescription);
                    }

                }, 1000); //
            }
        }
    }

    public interface BroadcastPlayerListener {
        void onEndOfBroadcast();

        void onServerError();

        void onServerContentError();

        void onServerGPSError();

        void onServerDescriptionUpdate(ServerDescription description);

        void onServerListUpdated();
    };
}
