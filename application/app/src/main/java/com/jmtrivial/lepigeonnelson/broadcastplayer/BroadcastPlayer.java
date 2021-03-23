package com.jmtrivial.lepigeonnelson.broadcastplayer;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class BroadcastPlayer extends HandlerThread {

    private final Context context;
    private int refreshDelay;
    private MessageCollector messageCollector;
    private MessagePlayer messagePlayer;
    private MessageQueue messageQueue;


    private UIHandler uiHandler;

    private ServerDescription currentServer;

    private boolean working;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BroadcastPlayer(Context context, int refreshDelay, UIHandler uiHandler) {
        super("BroadcastPlayer");
        this.context = context;
        this.refreshDelay = refreshDelay;
        SensorsService.getSensorsService(this.context).register(this);
        messagePlayer = null;
        messageCollector = null;
        messageQueue = null;
        working = false;

        this.uiHandler = uiHandler;
        init();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() {
        if (messagePlayer == null)
            messagePlayer = new MessagePlayer(context);
        if (messageQueue == null)
            messageQueue = new MessageQueue(messagePlayer, refreshDelay, uiHandler);
        if (messageCollector == null) {
            messageCollector = new MessageCollector(messageQueue, context, uiHandler);
            messageQueue.setCollector(messageCollector);
        }
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

    public boolean isPlaying() {
        return messagePlayer.isPlaying();
    }

    public void setListener(BroadcastPlayerListener listener) {
        uiHandler.setListener(listener);
    }

    public void checkSensorsSettings() {
        SensorsService service = SensorsService.getSensorsService(context);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void restartPlayer() {
        messagePlayer = new MessagePlayer(context);
    }

    public void startSensorService() {
        SensorsService service = SensorsService.getSensorsService(context);
        if (service != null) {
            service.startDataCollection();
        }
    }

    public void stopSensorService(boolean force) {
        if (force || !working) {
            SensorsService service = SensorsService.getSensorsService(context);
            if (service != null) {
                service.suspendDataCollection();
            }
        }
    }

    public void onSensorSettingsResult(int value) {
        Message msg = uiHandler.obtainMessage();
        msg.obj = value;
        msg.what = uiHandler.SENSOR_SETTINGS_RESULT;
        uiHandler.sendMessage(msg);
    }

    public interface BroadcastPlayerListener {
        void onEndOfBroadcast();

        void onServerError();

        void onServerContentError();

        void onServerGPSError();

        void onServerDescriptionUpdate(ServerDescription description);

        void onServerListUpdated();

        void onCurrentServerRequest(ServerDescription description);

        void onStatusPlaying();

        void onStatusNotPlaying();

        void onSensorSettingsInit(int error);
    };
}
