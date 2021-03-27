package fr.lepigeonnelson.player;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import fr.lepigeonnelson.player.R;

import fr.lepigeonnelson.player.broadcastplayer.BroadcastPlayer;
import fr.lepigeonnelson.player.broadcastplayer.ServerDescription;
import fr.lepigeonnelson.player.broadcastplayer.UIHandler;

public class PigeonNelsonService extends Service implements BroadcastPlayer.BroadcastPlayerListener {

    private static final int NOTIFICATION_ID = 1;

    private BroadcastPlayer player = null;
    private ServiceCallbacks serviceCallbacks = null;
    private PigeonNelsonService service = null;

    public static final String CHANNEL_ID = "BROADCAST";
    private static final String WAKELOCK_TAG = "lepigeonnelson:service";

    private static final int START_SERVICE = 1;
    private static final int COLLECT_SERVER_DESCRIPTION = 2;
    private static final int GET_CURRENT_SERVER = 3;
    private static final int SET_CURRENT_SERVER = 4;
    private static final int PLAY_BROADCAST = 5;
    private static final int STOP_BROADCAST = 6;
    private static final int RESET = 7;
    private static final int CHECK_SENSORS_SETTINGS = 8;
    private static final int STOP_SERVICE = 9;
    private static final int ASK_FOR_STATUS = 10;
    private static final int START_SENSOR_SERVICE = 11;
    private static final int STOP_SENSOR_SERVICE = 12;
    private static final int GET_PUBLIC_SERVERS = 13;


    private UIHandler uiHandler = null;

    // Binder given to clients
    private final IBinder binder = new PigeonNelsonBinder();
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private HandlerThread thread;
    private PowerManager.WakeLock wakeLock;
    private NotificationCompat.Builder builder;
    private Intent notifyIntent;
    private PendingIntent pendingIntent;

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void collectServerDescription(ServerDescription server) {
        Log.d("Service", "Collect server description for " + server.getUrl());
        Message msg = serviceHandler.obtainMessage();
        msg.what = COLLECT_SERVER_DESCRIPTION;
        msg.obj = server;
        serviceHandler.sendMessage(msg);
    }

    public void getCurrentServer() {
        Message msg = serviceHandler.obtainMessage();
        msg.what = GET_CURRENT_SERVER;
        serviceHandler.sendMessage(msg);
    }

    public void setCurrentServer(ServerDescription activeServer) {
        Message msg = serviceHandler.obtainMessage();
        msg.what = SET_CURRENT_SERVER;
        msg.obj = activeServer;
        serviceHandler.sendMessage(msg);
    }

    public void playBroadcast() {

        // force CPU to work on this service
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG);
        wakeLock.acquire();

        Message msg = serviceHandler.obtainMessage();
        msg.what = PLAY_BROADCAST;
        serviceHandler.sendMessage(msg);
    }

    public void stopBroadcast() {

        // stop forcing CPU to work on this service
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        Log.d("Service", "stop broadcast by message");
        Message msg = serviceHandler.obtainMessage();
        msg.what = STOP_BROADCAST;
        serviceHandler.sendMessage(msg);
    }

    public void reset() {
        Message msg = serviceHandler.obtainMessage();
        msg.what = RESET;
        serviceHandler.sendMessage(msg);
    }

    public void checkSensorsSettings() {
        Message msg = serviceHandler.obtainMessage();
        msg.what = CHECK_SENSORS_SETTINGS;
        serviceHandler.sendMessage(msg);
    }

    public void updateList() {
        uiHandler.sendEmptyMessage(uiHandler.UPDATE_LIST);
    }

    public void askForStatus() {
        Message msg = serviceHandler.obtainMessage();
        msg.what = ASK_FOR_STATUS;
        serviceHandler.sendMessage(msg);
    }

    public void getPublicServers() {
        Message msg = serviceHandler.obtainMessage();
        msg.what = GET_PUBLIC_SERVERS;
        serviceHandler.sendMessage(msg);
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class PigeonNelsonBinder extends Binder {
        PigeonNelsonService getService() {
            // Return this instance of PigeonNelsonService so clients can call public methods
            return PigeonNelsonService.this;
        }
    }


    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        createNotificationChannel();

        service = this;

        uiHandler = new UIHandler(getMainLooper());

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        // start the service
        Message msg = serviceHandler.obtainMessage();
        msg.what = START_SERVICE;
        serviceHandler.sendMessage(msg);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        notifyIntent = new Intent(this, MainActivity.class);
        // display a notification to the user
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(getBaseContext(),
                0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder
                // Add the metadata for the currently playing track
                .setContentTitle("Le Pigeon Nelson")
                .setOngoing(true)
                // Enable launching the player by clicking the notification
                .setContentIntent(pendingIntent)
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_le_pigeon_nelson_logo_icon);
            builder.setColor(getResources().getColor(R.color.ap_transparent));
        } else {
            builder.setSmallIcon(R.drawable.ic_le_pigeon_nelson_logo_icon);
        }
        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onDestroy() {
        Message msg = serviceHandler.obtainMessage();
        msg.what = STOP_SERVICE;
        serviceHandler.sendMessage(msg);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Message msg = serviceHandler.obtainMessage();
        msg.what = START_SENSOR_SERVICE;
        serviceHandler.sendMessage(msg);

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Message msg = serviceHandler.obtainMessage();
        msg.what = STOP_SENSOR_SERVICE;
        serviceHandler.sendMessage(msg);

        return false;
    }

    @Override
    public void onEndOfBroadcast() {
        if (serviceCallbacks != null)
            serviceCallbacks.onEndOfBroadcast();
    }
    @Override
    public void onServerError() {
        if (serviceCallbacks != null)
            serviceCallbacks.onServerError();
    }

    @Override
    public void onServerContentError() {
        if (serviceCallbacks != null)
            serviceCallbacks.onServerContentError();
    }

    @Override
    public void onServerGPSError() {
        if (serviceCallbacks != null)
            serviceCallbacks.onServerGPSError();
    }

    @Override
    public void onServerDescriptionUpdate(ServerDescription description) {
        if (serviceCallbacks != null)
            serviceCallbacks.onServerDescriptionUpdate(description);
    }

    @Override
    public void onCurrentServerRequest(ServerDescription description) {
        if (serviceCallbacks != null)
            serviceCallbacks.onCurrentServerRequest(description);
    }

    @Override
    public void onStatusPlaying() {
        if (serviceCallbacks != null)
            serviceCallbacks.onStatusPlaying();
    }

    @Override
    public void onStatusNotPlaying() {
        if (serviceCallbacks != null)
            serviceCallbacks.onStatusNotPlaying();
    }

    @Override
    public void onSensorSettingsInit(int result) {
        if (serviceCallbacks != null)
            serviceCallbacks.onSensorSettingsInit(result);
    }

    @Override
    public void onNewPublicServer(String url) {
        if (serviceCallbacks != null)
            serviceCallbacks.onNewPublicServer(url);
    }


    @Override
    public void onServerListUpdated() {
        if (serviceCallbacks != null)
            serviceCallbacks.onServerListUpdated();

    }

    public interface ServiceCallbacks {
        void onEndOfBroadcast();
         void onServerError();

        void onServerContentError();

        void onServerGPSError();

        void onServerDescriptionUpdate(ServerDescription description);

        void onServerListUpdated();

        void onCurrentServerRequest(ServerDescription description);

        void onStatusPlaying();

        void onStatusNotPlaying();

        void onSensorSettingsInit(int result);

        void onNewPublicServer(String url);
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private int id;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_SERVICE) {
                this.id = msg.arg1;

                player = new BroadcastPlayer(service, 100, uiHandler);
                player.setListener(service);
                player.start();
            }
            else if (msg.what == COLLECT_SERVER_DESCRIPTION) {
                ServerDescription desc = (ServerDescription) msg.obj;
                player.collectServerDescription(desc);
            }
            else if (msg.what == GET_CURRENT_SERVER) {
                Message nmsg = uiHandler.obtainMessage();
                nmsg.obj = player.getCurrentServer();
                nmsg.what = uiHandler.CURRENT_SERVER;
                uiHandler.sendMessage(nmsg);
            }
            else if (msg.what == SET_CURRENT_SERVER) {
                ServerDescription activeServer = (ServerDescription) msg.obj;
                player.setCurrentServer(activeServer);
            }
            else if (msg.what == GET_PUBLIC_SERVERS) {
                Log.d("PublicServerCollect", "receive request from main activity");
                player.getPublicServers();
            }
            else if (msg.what == PLAY_BROADCAST) {
                player.playBroadcast();
            }
            else if (msg.what == STOP_BROADCAST) {
                Log.d("Service", "Stop broadcast");
                player.stopBroadcast();
            }
            else if (msg.what == RESET) {
                player.reset();
            }
            else if (msg.what == CHECK_SENSORS_SETTINGS) {
                player.checkSensorsSettings();
            }
            else if (msg.what == ASK_FOR_STATUS) {
                if (player.isWorking()) {
                    Message nmsg = uiHandler.obtainMessage();
                    nmsg.what = uiHandler.STATUS_PLAYING;
                    uiHandler.sendMessage(nmsg);
                }
                else {
                    Message nmsg = uiHandler.obtainMessage();
                    nmsg.what = uiHandler.STATUS_NOT_PLAYING;
                    uiHandler.sendMessage(nmsg);
                }
            }
            else if (msg.what == START_SENSOR_SERVICE) {
                player.startSensorService();
            }
            else if (msg.what == STOP_SENSOR_SERVICE) {
                player.stopSensorService(false);
            }
            else if (msg.what == STOP_SERVICE) {
                Log.d("Service", "Stop service");
                stopBroadcast();
                reset();
                stopSelf(id);
            }

        }
    }
}
